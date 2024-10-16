from textblob import TextBlob
import sys
import json

def analyze_sentiment(text):
    blob = TextBlob(text)
    sentiment = blob.sentiment.polarity

    if sentiment > 0.5:
        return "happy and positive thoughts 💭"
    elif 0 < sentiment <= 0.5:
        return "feeling good with positivity 😌"
    elif sentiment == 0:
        return "normal 😐"
    elif sentiment < -0.5 and sentiment > -0.8:
        return "sad and depressed 😢"
    elif sentiment <= -0.8:
        return "angry and negativity 😡"
    else:
        return "neutral 😶"

if __name__ == "__main__":
    input_text = sys.argv[1]
    sentiment_result = analyze_sentiment(input_text)
    result = {"sentiment": sentiment_result}
    print(json.dumps(result))