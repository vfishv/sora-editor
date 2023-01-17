/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2023  Rosemoe
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
package io.github.rosemoe.sora.text;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.annotations.UnsupportedUserUsage;

/**
 * This is the listener for content.
 * You will receive calls when the Content object is going to change or changed.
 *
 * @author Rosemoe
 */
public interface ContentListener {

    /**
     * This is to notify you that you will receive {@link ContentListener#afterDelete(Content, int, int, int, int, CharSequence)}
     * and {@link ContentListener#afterInsert(Content, int, int, int, int, CharSequence)} calls.
     * These two calls are an action of replacement.
     * <strong> You are not expected to make changes to this Content object at this method. </strong>
     *
     * @param content The target Content object being replaced text
     */
    void beforeReplace(@NonNull Content content);

    /**
     * This is to notify you that the given Content object has inserted text with the given position and text
     *
     * @param content         The Content which has inserted the given text
     * @param startLine       The insertion position line
     * @param startColumn     The insertion position column on the line
     * @param endLine         The line position of the last character in inserted content
     * @param endColumn       The column position after the last character in inserted content
     * @param insertedContent The content inserted
     */
    void afterInsert(@NonNull Content content, int startLine, int startColumn, int endLine, int endColumn, @NonNull CharSequence insertedContent);

    /**
     * This is to notify you that the Content object has deleted the text with the given position.
     *
     * @param content        The Content which has deleted text
     * @param startLine      The start line of deleted text
     * @param startColumn    The start column of deleted text
     * @param endLine        The end line of deleted text
     * @param endColumn      The end column of deleted text(But this character not deleted)
     * @param deletedContent The text deleted.Generated by Content object.
     */
    void afterDelete(@NonNull Content content, int startLine, int startColumn, int endLine, int endColumn, @NonNull CharSequence deletedContent);

    /**
     * Internal API
     */
    @UnsupportedUserUsage
    default void beforeModification(@NonNull Content content) {

    }

}
