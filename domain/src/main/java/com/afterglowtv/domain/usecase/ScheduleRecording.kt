package com.afterglowtv.domain.usecase

import com.afterglowtv.domain.manager.RecordingManager
import com.afterglowtv.domain.model.Channel
import com.afterglowtv.domain.model.ContentType
import com.afterglowtv.domain.model.Program
import com.afterglowtv.domain.model.RecordingItem
import com.afterglowtv.domain.model.RecordingRecurrence
import com.afterglowtv.domain.model.RecordingRequest
import com.afterglowtv.domain.model.Result
import javax.inject.Inject

private const val MIN_PLACEHOLDER_RECORDING_MS = 60 * 60_000L

data class ScheduleRecordingCommand(
    val contentType: ContentType,
    val providerId: Long,
    val channel: Channel?,
    val streamUrl: String,
    val currentProgram: Program?,
    val nextProgram: Program?,
    val recurrence: RecordingRecurrence,
    val nowMs: Long = System.currentTimeMillis()
)

class ScheduleRecording @Inject constructor(
    private val recordingManager: RecordingManager
) {
    suspend operator fun invoke(command: ScheduleRecordingCommand): Result<RecordingItem> {
        val channel = command.channel
        val targetProgram = command.nextProgram ?: command.currentProgram

        if (
            command.contentType != ContentType.LIVE ||
            channel == null ||
            command.providerId <= 0L ||
            targetProgram == null ||
            command.streamUrl.isBlank()
        ) {
            return Result.error("Recording needs guide timing for the current live channel.")
        }

        val scheduledStartMs = maxOf(command.nowMs, targetProgram.startTime)
        val scheduledEndMs = resolveScheduledEndMs(targetProgram, scheduledStartMs)
        if (scheduledStartMs >= scheduledEndMs) {
            return Result.error("The selected program has already ended. Refresh the guide and try again.")
        }

        return recordingManager.scheduleRecording(
            RecordingRequest(
                providerId = command.providerId,
                channelId = channel.id,
                channelName = channel.name,
                streamUrl = command.streamUrl,
                scheduledStartMs = scheduledStartMs,
                scheduledEndMs = scheduledEndMs,
                programTitle = targetProgram.title,
                recurrence = command.recurrence
            )
        )
    }

    private fun resolveScheduledEndMs(program: Program, scheduledStartMs: Long): Long {
        if (!program.isPlaceholder) return program.endTime
        val minimumEndMs = scheduledStartMs + MIN_PLACEHOLDER_RECORDING_MS
        return maxOf(program.endTime, minimumEndMs)
    }
}
