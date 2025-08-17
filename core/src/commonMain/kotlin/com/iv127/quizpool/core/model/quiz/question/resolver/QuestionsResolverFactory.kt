package com.iv127.quizpool.core.model.quiz.question.resolver

import com.iv127.quizpool.core.model.quiz.question.Question
import org.intellij.markdown.MarkdownElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.CompositeASTNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

class QuestionsResolverFactory {

    fun create(type: QuestionsResolverType): QuestionsResolver {
        return when (type) {
            QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION
                -> getResolverForQuestionsWrappedInMarkdownCodeSection()
        }
    }

    private fun getResolverForQuestionsWrappedInMarkdownCodeSection(): QuestionsResolver {
        return object : QuestionsResolver {
            private val fenceChildrenTypesToInclude = setOf("EOL", "CODE_FENCE_CONTENT")

            override fun getType(): QuestionsResolverType {
                return QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION
            }

            override fun resolve(input: String): List<Question> {
                val flavour = CommonMarkFlavourDescriptor()
                val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(input)

                val fenceAstNodes = parsedTree.children.filter { astNode ->
                    astNode.type == MarkdownElementTypes.CODE_FENCE
                }

                return listOf()
            }

            private fun tryToConvertFenceAstNodeToQuestion(fenceNode: CompositeASTNode) : Question? {
                val fenceAstChildrenNodes = fenceNode.children.filter {
                    fenceChildrenTypesToInclude.contains(it.type.name)
                }

                return null
            }

        }
    }

}
