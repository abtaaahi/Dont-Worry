package com.abtahiapp.dontworry.query

object ArticleVideoQuery {

    private val angryQueries = listOf(
        "stress management", "anger management techniques", "how to calm down",
        "ways to handle stress", "managing emotions during stress", "deep breathing for stress relief",
        "stress relief activities", "dealing with frustration", "handling anxiety",
        "letting go of anger", "physical exercises for stress relief", "journaling for stress",
        "cognitive behavioral therapy for anger", "relaxation techniques", "progressive muscle relaxation",
        "anger triggers and how to control them", "developing emotional resilience", "stress and nutrition",
        "how to use humor to reduce stress", "mindfulness for anger", "anger and frustration management strategies",
        "building emotional intelligence", "how to calm down when angry quickly",
        "art therapy for anger", "how to stop overreacting", "anger management for kids and teens",
        "the role of exercise in anger control", "channeling anger through creativity",
        "understanding the root of anger", "anger and its effects on health", "anger control through breathing techniques",
        "managing anger in relationships", "how to prevent anger from escalating", "improving communication during anger"
    )

    private val sadQueries = listOf(
        "mental health awareness", "coping with sadness", "dealing with depression",
        "how to feel better emotionally", "mental wellness tips", "ways to improve mental health",
        "how to overcome sadness", "self-care for mental health", "talking to a therapist",
        "how to get out of a depressive state", "mental health exercises", "boosting emotional well-being",
        "fighting loneliness", "ways to lift your spirits", "healing from emotional pain",
        "strategies for mental resilience", "finding hope in tough times", "how to stop feeling down",
        "emotional healing from loss", "emotional burnout recovery", "overcoming negative thoughts",
        "how to rebuild self-esteem", "coping strategies for depression", "how to find happiness again",
        "activities for overcoming sadness", "how to reach out for emotional support", "how nature helps in dealing with sadness",
        "the importance of sleep for emotional health", "learning to accept and process grief", "reconnecting with loved ones during sadness",
        "creative outlets for emotional pain", "volunteering to improve mental health", "how small wins can improve mood",
        "building routines to overcome depression", "support groups for emotional health", "managing sadness through healthy habits"
    )

    private val fineQueries = listOf(
        "positive thinking techniques", "how to stay positive", "building a positive mindset",
        "ways to stay happy", "developing a positive attitude", "optimism and mental health",
        "gratitude practices", "mental health benefits of optimism", "positive lifestyle habits",
        "tips for being more optimistic", "how to cultivate joy", "how to find purpose in life",
        "embracing happiness in everyday life", "positive affirmations for mental health", "cultivating a mindset of abundance",
        "the science behind happiness", "boosting mood naturally", "self-love practices for happiness",
        "positive emotions and well-being", "building lasting happiness", "how to overcome negativity",
        "maintaining a positive work-life balance", "simple pleasures to improve mood",
        "daily habits for happiness", "how to sustain positivity", "the power of smiling for well-being",
        "mindful gratitude practices", "how helping others increases happiness", "positivity during difficult times",
        "the relationship between happiness and success", "small actions for big positivity shifts",
        "how hobbies improve mental well-being", "how to stay motivated in life", "finding happiness in personal achievements",
        "self-care routines for positivity", "developing long-term positivity habits", "simple morning routines for a positive day"
    )

    private val defaultQueries = listOf(
        "mindfulness meditation", "mindfulness techniques", "being present in the moment",
        "how to practice mindfulness", "mindful living tips", "benefits of mindfulness",
        "improving focus with mindfulness", "mindful breathing exercises", "meditation for peace",
        "daily mindfulness practices", "how to start a mindfulness routine", "mindful ways to reduce stress",
        "the power of mindfulness for mental clarity", "how to enhance mindfulness through yoga", "building awareness through mindfulness",
        "the science behind mindfulness", "how to practice self-compassion through mindfulness", "mindfulness and emotional regulation",
        "mindfulness techniques for anxiety", "mindfulness to improve sleep", "mindfulness for emotional balance",
        "how to cultivate mindful habits", "using mindfulness in daily life", "how mindfulness improves decision-making",
        "mindfulness for workplace stress", "creating a mindful home environment", "mindful walking techniques",
        "how to deal with distractions mindfully", "mindfulness for chronic pain", "simple mindfulness practices to start today",
        "how to teach mindfulness to children", "how to use mindfulness during a busy day", "mindful eating habits for better health",
        "improving relationships through mindfulness", "mindful time management", "how mindfulness boosts creativity"
    )

    fun getQueries(mood: String?): List<String> {
        return when (mood) {
            "Angry" -> angryQueries
            "Very Sad", "Sad" -> sadQueries
            "Fine", "Very Fine" -> fineQueries
            else -> defaultQueries
        }
    }
}