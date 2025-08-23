package com.iv127.quizflow.core.model.quiz.question.resolver

import com.iv127.quizflow.core.model.quiz.question.Question
import com.iv127.quizflow.core.model.quiz.question.lang.Outcome
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

internal class ResolverForQuestionsWrappedInMarkdownCodeSection : QuestionsResolver {
    companion object {
        private val FENCE_CHILDREN_TYPES_TO_INCLUDE = setOf("EOL", "CODE_FENCE_CONTENT")
        private val FORMAT_MESSAGE: String = """
            {Question} - text, can contain any amount of lines with text and new lines
            
            A. Answer 1
            B. Answer 2
            C. Answer 3
            <L>. Answer N - where L is an uppercased letter
            
            A, B, <L>. text, can contain any amount of lines with text without additional newlines
            Letters(question identifiers) should match question's letters
            """
    }

    override fun getType(): QuestionsResolverType {
        return QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION
    }

    override fun resolve(input: String): Outcome<List<Question>, QuestionsResolveException> {
        val flavour = CommonMarkFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(input)

        val fenceAstNodes = parsedTree.children.filter { astNode ->
            astNode.type == MarkdownElementTypes.CODE_FENCE
        }.filterIsInstance<CompositeASTNode>()

        val questionResults = fenceAstNodes.map {
            tryToConvertFenceAstNodeToQuestion(it, input)
        }

        if (questionResults.isEmpty()) {
            return Outcome.Failure(
                QuestionsResolveException(
                    QuestionsResolveException.Reason.NO_QUESTIONS_FOUND,
                    input,
                    "Can't find questions from specified source"
                )
            )
        }

        if (questionResults.last().isFailure) {
            val e = questionResults.last().exceptionOrNull() as QuestionsResolveException
            return Outcome.Failure(e)
        }

        return Outcome.Success(listOf())
    }

    private fun tryToConvertFenceAstNodeToQuestion(
        fenceNode: CompositeASTNode,
        rawMarkdown: CharSequence
    ): Result<Question> {
        return internalTryToConvertFenceAstNodeToQuestion(fenceNode, rawMarkdown)
    }


    private fun internalTryToConvertFenceAstNodeToQuestion(
        fenceNode: CompositeASTNode,
        rawMarkdown: CharSequence
    ): Result<Question> {
        val fenceAstChildrenNodes = fenceNode.children.filter {
            FENCE_CHILDREN_TYPES_TO_INCLUDE.contains(it.type.name)
        }.fold(mutableListOf<ASTNode>()) { acc, node ->
            acc.add(node)
            acc
        }

        while (fenceAstChildrenNodes.isNotEmpty() && isEOLASTNode(fenceAstChildrenNodes.first())) {
            fenceAstChildrenNodes.removeFirst()
        }
        while (fenceAstChildrenNodes.isNotEmpty() && isEOLASTNode(fenceAstChildrenNodes.last())) {
            fenceAstChildrenNodes.removeLast()
        }
        if (fenceAstChildrenNodes.isEmpty()) {
            return Result.failure(
                QuestionsResolveException(
                    QuestionsResolveException.Reason.INVALID_FORMAT,
                    fenceNode.getTextInNode(rawMarkdown).toString(),
                    FORMAT_MESSAGE
                )
            )
        }

        val dividedByDoubleEOLs = divideByDoubleEOLs(fenceAstChildrenNodes)
        if (dividedByDoubleEOLs.size < 3) {
            return Result.failure(
                QuestionsResolveException(
                    QuestionsResolveException.Reason.REQUIRED_SECTIONS_MISSED,
                    fenceNode.getTextInNode(rawMarkdown).toString(),
                    FORMAT_MESSAGE
                )
            )
        }

        val astNodesOfAnswersExplanation = dividedByDoubleEOLs.last()
        if (astNodesOfAnswersExplanation.isEmpty()) {
            return Result.failure(
                QuestionsResolveException(
                    QuestionsResolveException.Reason.REQUIRED_SECTIONS_MISSED,
                    fenceNode.getTextInNode(rawMarkdown).toString(),
                    "Answer explanation section is empty"
                )
            )
        }
        val correctAnswerLetters = extractCorrectAnswerLetters(astNodesOfAnswersExplanation.first(), rawMarkdown)
        val correctAnswerExplanation = astNodesOfAnswersExplanation.fold(StringBuilder()) { acc, node ->
            acc.append(node.getTextInNode(rawMarkdown))
            acc
        }.toString()

        val astNodesOfAnswers = dividedByDoubleEOLs.get(dividedByDoubleEOLs.size - 2)
        if (astNodesOfAnswers.isEmpty()) {
            return Result.failure(
                QuestionsResolveException(
                    QuestionsResolveException.Reason.REQUIRED_SECTIONS_MISSED,
                    fenceNode.getTextInNode(rawMarkdown).toString(),
                    "Answers section is empty"
                )
            )
        }
        val answerOptionsByLetters = extractAnswerOptionsByLetters(astNodesOfAnswers, rawMarkdown)
        if (answerOptionsByLetters.isFailure) {
            return Result.failure(answerOptionsByLetters.exceptionOrNull()!!)
        }

        return Result.failure(Exception())
    }

    private fun extractAnswerOptionsByLetters(
        astNodesOfAnswers: List<ASTNode>,
        rawMarkdown: CharSequence
    ): Result<Map<Char, String>> {
        val regex = """^([A-Z])(\.\s).*""".toRegex()
        val result = LinkedHashMap<Char, String>()
        for (answerNode in astNodesOfAnswers) {
            val answerText = answerNode.getTextInNode(rawMarkdown).toString()
            val matcher = regex.find(answerText)
            if (matcher != null) {
                val letter = matcher.groupValues[1].elementAt(0)
                val previousValue = result.put(letter, answerText)
                if (previousValue != null) {
                    return Result.failure(
                        QuestionsResolveException(
                            QuestionsResolveException.Reason.DUPLICATED_ANSWERS,
                            answerText,
                            """Duplicated letter: $letter"""
                        )
                    )
                }
            }
        }
        return Result.success(result)
    }

    private fun extractCorrectAnswerLetters(node: ASTNode, rawMarkdown: CharSequence): Set<Char> {
        val regex = """([A-Z])(,?\.?\s*)""".toRegex()
        val resultSet = LinkedHashSet<Char>()

        for (match in regex.findAll(node.getTextInNode(rawMarkdown))) {
            val character = match.groupValues[1].elementAt(0)
            resultSet.add(character)
            if (match.groupValues.size == 3 && match.groupValues[2].startsWith(".")) {
                break
            }
        }

        return resultSet
    }

    private fun divideByDoubleEOLs(fenceAstChildrenNodes: List<ASTNode>): List<List<ASTNode>> {
        val result = mutableListOf<MutableList<ASTNode>>(mutableListOf())
        val iterator = fenceAstChildrenNodes.iterator()
        var firstNode = iterator.next().let {
            result.last().add(it)
            it
        }
        while (iterator.hasNext()) {
            val current = iterator.next()
            if (isEOLASTNode(firstNode) && isEOLASTNode(current)) {
                result.add(mutableListOf())
                continue
            }
            result.last().add(current)
            firstNode = current
        }
        return result
    }

    private fun isEOLASTNode(node: ASTNode): Boolean {
        return node.type.name == "EOL"
    }
}
