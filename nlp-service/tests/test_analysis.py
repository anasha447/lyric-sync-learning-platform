from nlp.analysis import analyse_lines
from nlp.models import LineIn


def tokens_of(german, text: str):
    [line] = analyse_lines(german, [LineIn(index=0, text=text)])
    return line.tokens


def test_words_get_their_lemma_and_part_of_speech(german):
    tokens = tokens_of(german, "Ich ging nach Hause")
    assert [(t.text, t.lemma, t.pos) for t in tokens] == [
        ("Ich", "ich", "PRON"),
        ("ging", "gehen", "VERB"),
        ("nach", "nach", "ADP"),
        ("Hause", "Haus", "NOUN"),
    ]


def test_tokens_are_words_only_numbered_in_order(german):
    tokens = tokens_of(german, "Am Brunnen, vor dem Tore!")
    assert [t.text for t in tokens] == ["Am", "Brunnen", "vor", "dem", "Tore"]
    assert [t.index for t in tokens] == [0, 1, 2, 3, 4]


def test_an_instrumental_line_has_no_tokens(german):
    assert tokens_of(german, "") == []
    assert tokens_of(german, "   ") == []


def test_lines_keep_their_own_index_and_order(german):
    lines = analyse_lines(
        german,
        [
            LineIn(index=7, text="Da steht ein Lindenbaum"),
            LineIn(index=8, text="Ich träumt in seinem Schatten"),
        ],
    )
    assert [line.index for line in lines] == [7, 8]
    assert lines[0].tokens[-1].lemma == "Lindenbaum"
    assert lines[0].tokens[-1].pos == "NOUN"


def test_umlauts_and_eszett_survive(german):
    tokens = tokens_of(german, "So manchen süßen Traum")
    assert [t.text for t in tokens] == ["So", "manchen", "süßen", "Traum"]
