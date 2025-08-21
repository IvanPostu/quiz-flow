package com.iv127.quizflow.core.model.quiz.question.resolver

import com.iv127.quizflow.core.model.quiz.question.Question
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

    override fun resolve(input: String): List<Question> {
        val flavour = CommonMarkFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(input)

        val fenceAstNodes = parsedTree.children.filter { astNode ->
            astNode.type == MarkdownElementTypes.CODE_FENCE
        }.filterIsInstance<CompositeASTNode>()

        return fenceAstNodes.mapNotNull {
            tryToConvertFenceAstNodeToQuestion(it, input)
        }
    }

    private fun tryToConvertFenceAstNodeToQuestion(
        fenceNode: CompositeASTNode,
        rawMarkdown: CharSequence
    ): Question? {
        val fenceAstChildrenNodes = fenceNode.children.filter {
            FENCE_CHILDREN_TYPES_TO_INCLUDE.contains(it.type.name)
        }.fold(mutableListOf<ASTNode>()) { acc, node ->
            acc.add(node)
            acc
        }

        while (fenceAstChildrenNodes.isNotEmpty() && fenceAstChildrenNodes.first().type.name == "EOL") {
            fenceAstChildrenNodes.removeFirst()
        }
        while (fenceAstChildrenNodes.isNotEmpty() && fenceAstChildrenNodes.last().type.name == "EOL") {
            fenceAstChildrenNodes.removeLast()
        }

        fenceAstChildrenNodes
            .fold(StringBuilder()) { acc, el ->
                acc.append(el.getTextInNode(rawMarkdown))
            }.toString()

        return null
    }

    enum class ResolveState {

    }
}
