# Account and learner are separate

Identity owns the account (email, password hash, role, verification). The learning module owns the learner (level, daily goal, timezone, playback settings, saved words). A learner is created from the `AccountCreated` event and removed on `AccountDeleted`. This keeps identity free of learning concepts and lets account deletion cascade across services for GDPR.
