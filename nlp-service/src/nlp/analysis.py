"""Turning lyric lines into tokens. Pure: takes a loaded spaCy pipeline, knows nothing about HTTP."""

from collections.abc import Sequence

from spacy.language import Language
from spacy.tokens import Token as SpacyToken

from nlp.models import AnalyzedLine, LineIn, Token


def is_word(token: SpacyToken) -> bool:
    return not (token.is_punct or token.is_space)


def analyse_lines(nlp: Language, lines: Sequence[LineIn]) -> list[AnalyzedLine]:
    """Each line's word tokens with lemma and part of speech, in the order the lines came in.

    `nlp.pipe` processes the lines as a batch, which is much faster than one call per line.
    """
    docs = nlp.pipe(line.text for line in lines)
    return [
        AnalyzedLine(
            index=line.index,
            tokens=[
                Token(index=i, text=token.text, lemma=token.lemma_, pos=token.pos_)
                for i, token in enumerate(t for t in doc if is_word(t))
            ],
        )
        for line, doc in zip(lines, docs, strict=True)
    ]
