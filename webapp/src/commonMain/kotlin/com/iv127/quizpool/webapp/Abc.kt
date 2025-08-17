package com.iv127.quizpool.webapp

import com.iv127.quizpool.core.Greeting
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

class Abc {
    fun test() : String {
        val example: Greeting = Greeting()
        val src = "Some *Markdown*"
        val flavour = CommonMarkFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(src)
        val html = HtmlGenerator(src, parsedTree, flavour).generateHtml()
        return html + example.greet()
    }
}
