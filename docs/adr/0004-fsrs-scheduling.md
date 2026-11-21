# FSRS for review scheduling

Reviews are scheduled with FSRS using its published default parameters, not SM-2. FSRS needs noticeably fewer reviews for the same retention and has been Anki's default since 2023. The learner sees the same Again / Hard / Good / Easy buttons either way. The scheduler is pure domain code behind a `Scheduler` port, and every review is logged so parameters can be fitted per learner later.
