/*
 * Copyright (c) 2025, NVIDIA CORPORATION.
 *
 * This file was derived from OptimisticTransaction.scala and TransactionalWrite.scala
 * in the Delta Lake project at https://github.com/delta-io/delta.
 *
 * Copyright (2021) The Delta Lake Project Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.delta.hooks

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.delta._
import org.apache.spark.sql.delta.actions._
import org.apache.spark.sql.delta.rapids.GpuOptimisticTransactionBase

/**
 * Delta 3.3 version-specific implementation of GpuAutoCompact.
 * In Delta 3.3, the PostCommitHook.run method signature uses:
 * - OptimisticTransactionImpl instead of DeltaTransaction
 * - Seq[Action] instead of Iterator[Action]
 */
case object GpuAutoCompact extends GpuAutoCompactBase {

  override def run(
      spark: SparkSession,
      txn: OptimisticTransactionImpl,
      committedVersion: Long,
      postCommitSnapshot: Snapshot,
      actions: Seq[Action]): Unit = {
    run(spark, txn.asInstanceOf[GpuOptimisticTransactionBase],
      committedVersion, postCommitSnapshot, actions)
  }

  override def run(
      spark: SparkSession,
      txn: GpuOptimisticTransactionBase,
      committedVersion: Long,
      postCommitSnapshot: Snapshot,
      actions: Seq[Action]): Unit = {
    val conf = spark.sessionState.conf
    val autoCompactTypeOpt = getAutoCompactType(conf, postCommitSnapshot.metadata)
    // Skip Auto Compact if current transaction is not qualified or the table is not qualified
    // based on the value of autoCompactTypeOpt.
    if (shouldSkipAutoCompact(autoCompactTypeOpt, spark, txn)) return
    compactIfNecessary(
      spark,
      txn,
      postCommitSnapshot,
      OP_TYPE,
      maxDeletedRowsRatio = None)
  }
}

