// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.daemon.impl;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactory;
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class CodeFoldingPassFactory implements TextEditorHighlightingPassFactory {
  public CodeFoldingPassFactory(TextEditorHighlightingPassRegistrar highlightingPassRegistrar) {
    highlightingPassRegistrar.registerTextEditorHighlightingPass(this, null, null, false, Pass.UPDATE_FOLDING);
  }

  @Override
  @NotNull
  public TextEditorHighlightingPass createHighlightingPass(@NotNull PsiFile file, @NotNull final Editor editor) {
    return new CodeFoldingPass(editor, file);
  }
}
