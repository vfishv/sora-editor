/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2022  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 */
package io.github.rosemoe.sora.langs.java;

import static io.github.rosemoe.sora.lang.styling.TextStyle.makeStyle;

import java.util.Stack;

import io.github.rosemoe.sora.lang.analysis.SimpleAnalyzeManager;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.styling.CodeBlock;
import io.github.rosemoe.sora.lang.styling.MappedSpans;
import io.github.rosemoe.sora.lang.styling.Styles;
import io.github.rosemoe.sora.lang.styling.TextStyle;
import io.github.rosemoe.sora.text.LineNumberCalculator;
import io.github.rosemoe.sora.util.TrieTree;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * Note:Navigation not supported
 *
 * @author Rose
 */
public class JavaAnalyzeManager extends SimpleAnalyzeManager<IdentifierAutoComplete.Identifiers> {

    private final static Object OBJECT = new Object();

    @Override
    protected Styles analyze(StringBuilder text, Delegate<IdentifierAutoComplete.Identifiers> delegate) {
        var tokenizer = new JavaTextTokenizer(text);
        var styles = new Styles();
        var result = new MappedSpans.Builder(1024);
        tokenizer.setCalculateLineColumn(false);

        Tokens token, previous = Tokens.UNKNOWN;
        int line = 0, column = 0;
        var helper = new LineNumberCalculator(text);
        var identifiers = new IdentifierAutoComplete.Identifiers();
        identifiers.begin();

        var stack = new Stack<CodeBlock>();
        int maxSwitch = 1, currSwitch = 0;
        //Tree to save class names and query
        TrieTree<Object> classNames = new TrieTree<>();
        //Whether previous token is class name
        boolean classNamePrevious = false;
        //Add default class name
        classNames.put("String", OBJECT);
        classNames.put("Object", OBJECT);
        boolean first = true;
        while (!delegate.isCancelled()) {
            try {
                // directNextToken() does not skip any token
                token = tokenizer.directNextToken();
            } catch (RuntimeException e) {
                //When a spelling input is in process, this will happen because of format mismatch
                token = Tokens.CHARACTER_LITERAL;
            }
            if (token == Tokens.EOF) {
                break;
            }
            // Backup values because looking ahead in function name match will change them
            int thisIndex = tokenizer.getIndex();
            int thisLength = tokenizer.getTokenLength();
            switch (token) {
                case WHITESPACE:
                case NEWLINE:
                    if (first) {
                        result.addNormalIfNull();
                    }
                    break;
                case IDENTIFIER:
                    //Add a identifier to auto complete
                    identifiers.addIdentifier(text.substring(tokenizer.getIndex(), tokenizer.getTokenLength() + tokenizer.getIndex()));
                    //The previous so this will be the annotation's type name
                    if (previous == Tokens.AT) {
                        result.addIfNeeded(line, column, EditorColorScheme.ANNOTATION);
                        break;
                    }
                    //Here we have to get next token to see if it is function
                    //We can only get the next token in stream.
                    //If more tokens required, we have to use a stack in tokenizer
                    Tokens next;
                    try {
                        next = tokenizer.directNextToken();
                    } catch (RuntimeException e) {
                        next = Tokens.UNKNOWN;
                    }
                    //The next is LPAREN,so this is function name or type name
                    if (next == Tokens.LPAREN) {
                        result.addIfNeeded(line, column, EditorColorScheme.FUNCTION_NAME);
                        tokenizer.pushBack(tokenizer.getTokenLength());
                        break;
                    }
                    //Push back the next token
                    tokenizer.pushBack(tokenizer.getTokenLength());
                    //This is a class definition
                    if (previous == Tokens.CLASS) {
                        result.addIfNeeded(line, column, EditorColorScheme.IDENTIFIER_NAME);
                        //Add class name
                        classNames.put(text, thisIndex, thisLength, OBJECT);
                        break;
                    }
                    //Has class name
                    if (classNames.get(text, thisIndex, thisLength) == OBJECT) {
                        result.addIfNeeded(line, column, EditorColorScheme.IDENTIFIER_NAME);
                        //Mark it
                        classNamePrevious = true;
                        break;
                    }
                    if (classNamePrevious) {
                        //Var name
                        result.addIfNeeded(line, column, EditorColorScheme.IDENTIFIER_VAR);
                        classNamePrevious = false;
                        break;
                    }
                    result.addIfNeeded(line, column, EditorColorScheme.TEXT_NORMAL);
                    break;
                case CHARACTER_LITERAL:
                case FLOATING_POINT_LITERAL:
                case INTEGER_LITERAL:
                    classNamePrevious = false;
                    result.addIfNeeded(line, column, EditorColorScheme.LITERAL);
                    break;
                case STRING:
                    classNamePrevious = false;
                    result.addIfNeeded(line, column, TextStyle.makeStyle(EditorColorScheme.LITERAL, true));
                    break;
                case INT:
                case LONG:
                case BOOLEAN:
                case BYTE:
                case CHAR:
                case FLOAT:
                case DOUBLE:
                case SHORT:
                case VOID:
                    classNamePrevious = true;
                    result.addIfNeeded(line, column, makeStyle(EditorColorScheme.KEYWORD, 0, true, false, false));
                    break;
                case ABSTRACT:
                case ASSERT:
                case CLASS:
                case DO:
                case FINAL:
                case FOR:
                case IF:
                case NEW:
                case PUBLIC:
                case PRIVATE:
                case PROTECTED:
                case PACKAGE:
                case RETURN:
                case STATIC:
                case SUPER:
                case SWITCH:
                case ELSE:
                case VOLATILE:
                case SYNCHRONIZED:
                case STRICTFP:
                case GOTO:
                case CONTINUE:
                case BREAK:
                case TRANSIENT:
                case TRY:
                case CATCH:
                case FINALLY:
                case WHILE:
                case CASE:
                case DEFAULT:
                case CONST:
                case ENUM:
                case EXTENDS:
                case IMPLEMENTS:
                case IMPORT:
                case INSTANCEOF:
                case INTERFACE:
                case NATIVE:
                case THIS:
                case THROW:
                case THROWS:
                case TRUE:
                case FALSE:
                case NULL:
                case VAR:
                case SEALED:
                case PERMITS:
                    classNamePrevious = false;
                    result.addIfNeeded(line, column, makeStyle(EditorColorScheme.KEYWORD, 0, true, false, false));
                    break;
                case LBRACE: {
                    classNamePrevious = false;
                    result.addIfNeeded(line, column, EditorColorScheme.OPERATOR);
                    if (stack.isEmpty()) {
                        if (currSwitch > maxSwitch) {
                            maxSwitch = currSwitch;
                        }
                        currSwitch = 0;
                    }
                    currSwitch++;
                    CodeBlock block = styles.obtainNewBlock();
                    block.startLine = line;
                    block.startColumn = column;
                    stack.push(block);
                    break;
                }
                case RBRACE: {
                    classNamePrevious = false;
                    result.addIfNeeded(line, column, EditorColorScheme.OPERATOR);
                    if (!stack.isEmpty()) {
                        CodeBlock block = stack.pop();
                        block.endLine = line;
                        block.endColumn = column;
                        if (block.startLine != block.endLine) {
                            styles.addCodeBlock(block);
                        }
                    }
                    break;
                }
                case LINE_COMMENT:
                case LONG_COMMENT:
                    result.addIfNeeded(line, column, makeStyle(EditorColorScheme.COMMENT, true));
                    break;
                default:
                    if (token == Tokens.LBRACK || (token == Tokens.RBRACK && previous == Tokens.LBRACK)) {
                        result.addIfNeeded(line, column, EditorColorScheme.OPERATOR);
                        break;
                    }
                    classNamePrevious = false;
                    result.addIfNeeded(line, column, EditorColorScheme.OPERATOR);
            }
            first = false;
            helper.update(thisLength);
            line = helper.getLine();
            column = helper.getColumn();
            if (token != Tokens.WHITESPACE && token != Tokens.NEWLINE) {
                previous = token;
            }
        }
        if (stack.isEmpty()) {
            if (currSwitch > maxSwitch) {
                maxSwitch = currSwitch;
            }
        }
        identifiers.finish();
        result.determine(line);
        delegate.setData(identifiers);
        styles.setSuppressSwitch(maxSwitch + 10);
        styles.spans = result.build();
        return styles;
    }

}
