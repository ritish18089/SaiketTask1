import json
import random

def process_questions():
    input_path = 'app/src/main/assets/questions.json'
    with open(input_path, 'r', encoding='utf-8') as f:
        questions = json.load(f)

    # To ensure even distribution, we cycle through positions 0, 1, 2, 3
    # We also want to randomize the starting point or shuffle the list first?
    # Shuffling the list first might be good, but the user said "Keep original question exactly the same"
    # and "Preserve all other fields... id". I'll keep the order but randomize the positions.

    # Actually, to make it feel random but even, I'll shuffle the sequence of target indices
    total = len(questions)
    target_indices = ([0, 1, 2, 3] * (total // 4 + 1))[:total]
    random.shuffle(target_indices)

    processed = []
    for i, q in enumerate(questions):
        correct = q['correctAnswer']
        options = q['options']

        # Ensure correct is in options
        if correct not in options:
            # If not found, we have a problem. But in the current file they match.
            # We'll assume the current file is mostly okay for 'options'.
            pass

        # Filter out duplicates and the correct answer
        other_options = [opt for opt in options if opt != correct]

        # If there were duplicates of the correct answer, we might have fewer than 3 others
        # We need exactly 4 options.
        while len(other_options) < 3:
            # Add some dummy options or modified ones if needed?
            # The prompt says "Keep the same four options unless an option is incorrect"
            # For now let's assume they are unique.
            other_options.append(f"Option {len(other_options)}")

        # Limit to 3
        other_options = other_options[:3]
        random.shuffle(other_options)

        target_pos = target_indices[i]
        new_options = other_options[:target_pos] + [correct] + other_options[target_pos:]

        q['options'] = new_options
        q['correctAnswer'] = correct # Remains same string

        # QA Check
        assert len(new_options) == 4
        assert new_options[target_pos] == correct
        assert q['correctAnswer'] in q['options']

        processed.append(q)

    with open('questions_fixed.json', 'w', encoding='utf-8') as f:
        json.dump(processed, f, indent=4)

if __name__ == "__main__":
    process_questions()
