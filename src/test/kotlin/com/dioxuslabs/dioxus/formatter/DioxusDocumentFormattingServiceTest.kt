package com.dioxuslabs.dioxus.formatter

import com.intellij.psi.formatter.FormatterTestCase

class DioxusDocumentFormattingServiceTest : FormatterTestCase() {
    override fun getTestDataPath(): String = "src/test/testData"

    override fun getBasePath(): String = "formatting"

    override fun getFileExtension(): String = "rs"

    // Tests below this line

    // If you want to use bigger files to format use the following
    // Files are stored in src/test/testData/formatting
    //    fun testBigFile() {
    //        doTest("bigFile.rs", "bigFileAfter.rs")
    //    }

    fun testBasicFormatting() {
        // Test basic RSX formatting with spaces
        val unformattedCode = """
            fn main() {
                let app = rsx! {
                    div {
                    h1 { "Hello, world!" }
                        p { "This is a test." }
                    }
                };
            }
        """.trimIndent()

        val expectedFormattedCode = """
            fn main() {
                let app = rsx! {
                    div {
                        h1 { "Hello, world!" }
                        p { "This is a test." }
                    }
                };
            }
        """.trimIndent()

        doTextTest(unformattedCode, expectedFormattedCode)
    }
}