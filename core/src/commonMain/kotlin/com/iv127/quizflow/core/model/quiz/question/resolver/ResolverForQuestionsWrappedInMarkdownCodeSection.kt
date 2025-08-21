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

        return Outcome.Success(listOf())
    }

    private fun tryToConvertFenceAstNodeToQuestion(
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
            return Result.failure(Exception())
        }

        val dividedByDoubleEOLs = divideByDoubleEOLs(fenceAstChildrenNodes)

        fenceAstChildrenNodes.asReversed()
            .fold(StringBuilder()) { acc, el ->
                acc.append(el.getTextInNode(rawMarkdown))
            }.toString()

        return Result.failure(Exception())
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

    private data class CorrectAnswerExplanation(val explanation: String, val correctAnswerIds: Set<Char>);

    private fun isEOLASTNode(node: ASTNode): Boolean {
        return node.type.name == "EOL"
    }
}
