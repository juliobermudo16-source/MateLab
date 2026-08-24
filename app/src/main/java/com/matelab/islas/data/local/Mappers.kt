package com.matelab.islas.data.local

import com.matelab.islas.data.local.entity.BadgeEntity
import com.matelab.islas.data.local.entity.ChallengeEntity
import com.matelab.islas.data.local.entity.CollectibleEntity
import com.matelab.islas.data.local.entity.MissionEntity
import com.matelab.islas.data.local.entity.MissionProgressEntity
import com.matelab.islas.data.local.entity.ProfileEntity
import com.matelab.islas.data.local.entity.ReviewItemEntity
import com.matelab.islas.data.local.entity.SettingsEntity
import com.matelab.islas.data.local.entity.WorldEntity
import com.matelab.islas.domain.model.Badge
import com.matelab.islas.domain.model.BadgeRule
import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.Collectible
import com.matelab.islas.domain.model.Difficulty
import com.matelab.islas.domain.model.GameKind
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.MissionProgress
import com.matelab.islas.domain.model.MissionStatus
import com.matelab.islas.domain.model.Profile
import com.matelab.islas.domain.model.QuizPayload
import com.matelab.islas.domain.model.Rarity
import com.matelab.islas.domain.model.ReviewItem
import com.matelab.islas.domain.model.Settings
import com.matelab.islas.domain.model.World
import com.matelab.islas.domain.model.WorldTheme

/**
 * Conversion entre las filas de Room y los modelos de dominio.
 * Se mantiene manual y explicita para que un cambio de esquema salte a la vista.
 */

fun WorldEntity.toDomain(): World = World(
    id = id,
    order = orderIndex,
    name = name,
    subtitle = subtitle,
    description = description,
    theme = runCatching { WorldTheme.valueOf(theme) }.getOrDefault(WorldTheme.FORMAS),
    xpToUnlock = xpToUnlock
)

fun World.toEntity(): WorldEntity = WorldEntity(
    id = id,
    orderIndex = order,
    name = name,
    subtitle = subtitle,
    description = description,
    theme = theme.name,
    xpToUnlock = xpToUnlock
)

fun MissionEntity.toDomain(): Mission = Mission(
    id = id,
    worldId = worldId,
    order = orderIndex,
    name = name,
    goal = goal,
    briefing = briefing,
    difficulty = runCatching { Difficulty.valueOf(difficulty) }.getOrDefault(Difficulty.EXPLORADOR),
    requires = if (requires.isBlank()) emptyList() else requires.split(","),
    rewardCollectibleId = rewardCollectibleId
)

fun Mission.toEntity(): MissionEntity = MissionEntity(
    id = id,
    worldId = worldId,
    orderIndex = order,
    name = name,
    goal = goal,
    briefing = briefing,
    difficulty = difficulty.name,
    requires = requires.joinToString(","),
    rewardCollectibleId = rewardCollectibleId
)

fun ChallengeEntity.toDomain(): Challenge = Challenge(
    id = id,
    missionId = missionId,
    order = orderIndex,
    kind = runCatching { GameKind.valueOf(kind) }.getOrDefault(GameKind.QUIZ),
    prompt = prompt,
    explanation = explanation,
    hint = hint,
    payload = MateJson.decodeOrNull(payloadJson)
        ?: QuizPayload(listOf("Continuar"), 0),
    xp = xp
)

fun Challenge.toEntity(): ChallengeEntity = ChallengeEntity(
    id = id,
    missionId = missionId,
    orderIndex = order,
    kind = kind.name,
    prompt = prompt,
    explanation = explanation,
    hint = hint,
    payloadJson = MateJson.encode(payload),
    xp = xp
)

fun BadgeEntity.toDomain(): Badge = Badge(
    id = id,
    name = name,
    description = description,
    rule = runCatching { BadgeRule.valueOf(rule) }.getOrDefault(BadgeRule.FIRST_MISSION),
    threshold = threshold,
    param = param,
    artSeed = artSeed
)

fun Badge.toEntity(): BadgeEntity = BadgeEntity(
    id = id,
    name = name,
    description = description,
    rule = rule.name,
    threshold = threshold,
    param = param,
    artSeed = artSeed
)

fun CollectibleEntity.toDomain(): Collectible = Collectible(
    id = id,
    name = name,
    fact = fact,
    worldId = worldId,
    rarity = runCatching { Rarity.valueOf(rarity) }.getOrDefault(Rarity.COMUN),
    artSeed = artSeed
)

fun Collectible.toEntity(): CollectibleEntity = CollectibleEntity(
    id = id,
    name = name,
    fact = fact,
    worldId = worldId,
    rarity = rarity.name,
    artSeed = artSeed
)

fun ProfileEntity.toDomain(crystals: Int): Profile = Profile(
    alias = alias,
    avatarId = avatarId,
    xp = xp,
    crystals = crystals,
    streakDays = streakDays,
    lastPlayedDay = lastPlayedDay,
    onboardingDone = onboardingDone,
    profileDone = profileDone
)

fun SettingsEntity.toDomain(): Settings = Settings(
    soundEnabled = soundEnabled,
    hapticsEnabled = hapticsEnabled,
    animationsEnabled = animationsEnabled,
    bigTextEnabled = bigTextEnabled
)

fun Settings.toEntity(): SettingsEntity = SettingsEntity(
    id = 1,
    soundEnabled = soundEnabled,
    hapticsEnabled = hapticsEnabled,
    animationsEnabled = animationsEnabled,
    bigTextEnabled = bigTextEnabled
)

fun MissionProgressEntity.toDomain(): MissionProgress = MissionProgress(
    missionId = missionId,
    status = runCatching { MissionStatus.valueOf(status) }.getOrDefault(MissionStatus.DISPONIBLE),
    stars = stars,
    bestPercent = bestPercent,
    timesPlayed = timesPlayed,
    lastPlayedAt = lastPlayedAt
)

fun ReviewItemEntity.toDomain(): ReviewItem = ReviewItem(
    challengeId = challengeId,
    missionId = missionId,
    worldId = worldId,
    wrongCount = wrongCount,
    lastWrongAt = lastWrongAt,
    resolved = resolved
)
