# lyricsync

English speakers learn German by listening to songs with synced, tappable lyrics and by reviewing the words they save along the way.

## Catalog

**Song**:
A recording with its timed lyrics, artist, level and license that learners play.
_Avoid_: Track, content

**Cover**:
The square picture shown for a song: uploaded by an admin, or generated from the song's title when none is uploaded.
_Avoid_: Album art, thumbnail

**Line**:
One timed lyric line of a song, with a start time, an end time and an optional line translation.
_Avoid_: Lyric line, song line, verse

**Instrumental line**:
A timed line without words.

**Token**:
One word occurrence at a fixed position inside a line.
_Avoid_: Word (when the occurrence is meant)

**Line translation**:
The English rendering of a whole line, shown only when the learner asks for it.
_Avoid_: Subtitle

**Ingestion**:
Turning an uploaded audio file and its `.lrc` text into a song with lines.
_Avoid_: Import, upload (upload is only the transfer)

**Song status**:
Where a song is in its lifecycle: Draft, Processing, Ready for review, Published or Failed.

**Draft**:
A song that is stored but not yet sent for analysis.

**Ready for review**:
A song whose analysis finished and which waits for an admin to resolve unmatched tokens.

**Publishing**:
Making a song visible in the library. Needs a level and no unmatched tokens.

**Level**:
The CEFR difficulty of a song, or the level a learner studies at: A1, A2, B1 or B2.
_Avoid_: Difficulty, grade (a grade is a review rating)

**License**:
The legal basis for showing a song: public domain or CC0, CC BY, CC BY-SA, own recording, or written permission.

## Dictionary

**Dictionary entry**:
A headword with its part of speech, gender for nouns, and English meanings.
_Avoid_: Word, vocab, definition

**Lemma**:
The base form of a word, such as *gehen* for *ging*.
_Avoid_: Root, stem, infinitive

**Form**:
An inflected spelling that points to a dictionary entry, such as *ging* → *gehen*.

**Gender**:
Masculine, feminine or neuter, the grammatical gender of a noun.
_Avoid_: Article (the article is how gender is shown: der, die, das)

**Separable verb**:
A verb whose prefix splits off in a sentence, such as *rufe … an* → *anrufen*.

**Partner token**:
The other half of a split separable verb in the same line.

**Match status**:
Whether a token is linked to a dictionary entry (Matched) or still needs an admin (Unmatched).

## People

**Account**:
A person's login identity: email, password and role.
_Avoid_: User (ambiguous), profile

**Learner**:
The learning side of an account: display name, level, learning reasons, daily goal, timezone, saved words and progress.
_Avoid_: User, student

**Display name**:
The optional name a learner wants to be called, used to greet them.
_Avoid_: Username (logins use email; there are no usernames)

**Onboarding**:
The four questions on a first visit that set up a guest or learner: display name, level, learning reasons and daily goal.

**Learning reason**:
One of the reasons a learner gave for learning German; a learner can have several.

**Guest**:
A visitor learning without an account; their onboarding answers and completions live only in their browser until they sign up.

**Admin**:
An account allowed to ingest, review and publish songs.

## Learning

**Saved word**:
A dictionary entry a learner chose to review. A learner has at most one saved word per dictionary entry.
_Avoid_: Vocab item, card, flashcard

**Sighting**:
One place a saved word was saved from: a song, a line and a token. A saved word collects sightings.

**Review**:
One rating of a saved word, replaying the line of one of its sightings.
_Avoid_: Card (UI only), quiz

**Grade**:
The learner's rating in a review: Again, Hard, Good or Easy.

**Due**:
A saved word whose next review time has passed.

**Completion**:
A learner played a song to its end at least once.

**Daily goal**:
The number of reviews a learner aims for each day, chosen during onboarding as minutes per day (5, 10, 15 or 20 minutes for 10, 20, 30 or 40 reviews).

**Streak**:
Consecutive days, in the learner's timezone, on which the daily goal was met, or everything due was reviewed and a song was completed.
