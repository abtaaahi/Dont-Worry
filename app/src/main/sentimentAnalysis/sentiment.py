from textblob import TextBlob
import sys
import json

def analyze_sentiment(text):
    blob = TextBlob(text)
    sentiment = blob.sentiment.polarity
    if sentiment > 0:
        return "positive"
    elif sentiment == 0:
        return "neutral"
    else:
        return "negative"

if __name__ == "__main__":
    input_text = sys.argv[1]
    sentiment_result = analyze_sentiment(input_text)
    result = {"sentiment": sentiment_result}
    print(json.dumps(result))