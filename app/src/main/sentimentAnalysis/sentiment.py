from textblob import TextBlob
import sys
import json

def analyze_sentiment(text):
    blob = TextBlob(text)
    sentiment = blob.sentiment.polarity

    if sentiment >= 0.5:
        sentiment_label = "Very Happy 😄"
    elif 0.2 <= sentiment < 0.5:
        sentiment_label = "Feeling Positive & Happy 😌"
    elif 0 < sentiment < 0.2:
        sentiment_label = "Slightly Positive 🙂"
    elif sentiment == 0:
        sentiment_label = "Neutral 😐"
    elif -0.2 < sentiment < 0:
        sentiment_label = "Slightly Negative 😕"
    elif -0.5 <= sentiment <= -0.2:
        sentiment_label = "Feeling Sad 😢"
    elif sentiment < -0.5:
        sentiment_label = "Very Negative / Angry 😡"

    return sentiment_label, str(sentiment)

if __name__ == "__main__":
    input_text = sys.argv[1]
    sentiment_result, sentiment_score = analyze_sentiment(input_text)

    result = {
        "sentiment": sentiment_result,
        "score": sentiment_score
    }

    print(json.dumps(result))