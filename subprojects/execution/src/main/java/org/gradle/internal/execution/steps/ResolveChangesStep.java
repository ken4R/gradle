/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.execution.steps;

import org.gradle.internal.execution.IncrementalChangesContext;
import org.gradle.internal.execution.IncrementalContext;
import org.gradle.internal.execution.Result;
import org.gradle.internal.execution.Step;
import org.gradle.internal.execution.UnitOfWork;
import org.gradle.internal.execution.history.AfterPreviousExecutionState;
import org.gradle.internal.execution.history.BeforeExecutionState;
import org.gradle.internal.execution.history.changes.ExecutionStateChangeDetector;
import org.gradle.internal.execution.history.changes.ExecutionStateChanges;
import org.gradle.internal.execution.history.changes.RebuildExecutionStateChanges;

import java.util.Optional;

public class ResolveChangesStep<R extends Result> implements Step<IncrementalContext, R> {
    private final ExecutionStateChangeDetector changeDetector;
    private static final String NO_HISTORY = "No history is available.";

    private final Step<? super IncrementalChangesContext, R> delegate;

    public ResolveChangesStep(
        ExecutionStateChangeDetector changeDetector,
        Step<? super IncrementalChangesContext, R> delegate
    ) {
        this.changeDetector = changeDetector;
        this.delegate = delegate;
    }

    @Override
    public R execute(IncrementalContext context) {
        UnitOfWork work = context.getWork();
        Optional<BeforeExecutionState> beforeExecutionState = context.getBeforeExecutionState();
        ExecutionStateChanges changes = context.getRebuildReason()
            .<ExecutionStateChanges>map(rebuildReason ->
                new RebuildExecutionStateChanges(rebuildReason, beforeExecutionState
                    .map(beforeExecution -> beforeExecution.getInputFileProperties())
                    .orElse(null))
            )
            .orElseGet(() ->
                beforeExecutionState
                    .map(beforeExecution -> context.getAfterPreviousExecutionState()
                        .map(afterPreviousExecution -> changeDetector.detectChanges(
                            afterPreviousExecution,
                            beforeExecution,
                            work,
                            !work.isAllowOverlappingOutputs())
                        )
                        .orElseGet(() -> new RebuildExecutionStateChanges(NO_HISTORY, beforeExecution.getInputFileProperties()))
                    )
                    .orElse(null)
            );

        return delegate.execute(new IncrementalChangesContext() {
            @Override
            public Optional<ExecutionStateChanges> getChanges() {
                return Optional.ofNullable(changes);
            }

            @Override
            public Optional<String> getRebuildReason() {
                return context.getRebuildReason();
            }

            @Override
            public Optional<AfterPreviousExecutionState> getAfterPreviousExecutionState() {
                return context.getAfterPreviousExecutionState();
            }

            @Override
            public Optional<BeforeExecutionState> getBeforeExecutionState() {
                return beforeExecutionState;
            }

            @Override
            public UnitOfWork getWork() {
                return work;
            }
        });
    }

}