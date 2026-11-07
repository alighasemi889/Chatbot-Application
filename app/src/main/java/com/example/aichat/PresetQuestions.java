package com.example.aichat;

import java.util.ArrayList;
import java.util.List;

/** Ready-to-send questions grouped by topic. Each question has a Persian and English version. */
public final class PresetQuestions {

    public static final class Question {
        public final String fa;
        public final String en;

        Question(String fa, String en) {
            this.fa = fa;
            this.en = en;
        }
    }

    public static final class Category {
        public final String nameFa;
        public final String nameEn;
        public final List<Question> questions;

        Category(String nameFa, String nameEn, List<Question> questions) {
            this.nameFa = nameFa;
            this.nameEn = nameEn;
            this.questions = questions;
        }
    }

    private static final List<Category> CATEGORIES = new ArrayList<>();

    static {
        CATEGORIES.add(new Category("برنامه‌نویسی", "Programming", list(
                new Question("برنامه‌نویسی شیءگرا چیست؟", "What is object-oriented programming?"),
                new Question("تفاوت متغیر و ثابت چیست؟", "What is the difference between a variable and a constant?"),
                new Question("بازگشتی (Recursion) را با یک مثال توضیح بده.", "Explain recursion with an example."),
                new Question("یک تابع برای مرتب‌سازی یک لیست بنویس.", "Write a function that sorts a list."),
                new Question("چطور باگ‌ها را سریع‌تر پیدا کنم؟", "How can I find bugs faster?"))));

        CATEGORIES.add(new Category("فناوری", "Technology", list(
                new Question("Cloud Computing چیست؟", "What is cloud computing?"),
                new Question("تفاوت SSD و HDD چیست؟", "What is the difference between an SSD and an HDD?"),
                new Question("API چیست و چرا مهم است؟", "What is an API and why does it matter?"),
                new Question("کانتینر Docker چیست؟", "What is a Docker container?"))));

        CATEGORIES.add(new Category("امنیت سایبری", "Cybersecurity", list(
                new Question("چطور از حملات فیشینگ جلوگیری کنم؟", "How do I protect myself from phishing?"),
                new Question("VPN چه کاری انجام می‌دهد؟", "What does a VPN do?"),
                new Question("یک رمز عبور امن چطور ساخته می‌شود؟", "How do I create a strong password?"),
                new Question("Zero Trust چیست؟", "What is Zero Trust?"))));

        CATEGORIES.add(new Category("هوش مصنوعی", "Artificial Intelligence", list(
                new Question("یادگیری ماشین و یادگیری عمیق چه تفاوتی دارند؟",
                        "What is the difference between machine learning and deep learning?"),
                new Question("Prompt Engineering چیست؟", "What is prompt engineering?"),
                new Question("Overfitting را توضیح بده.", "Explain overfitting."),
                new Question("نقشه راه یادگیری هوش مصنوعی چیست؟", "What is a learning roadmap for AI?"))));
    }

    private PresetQuestions() {}

    private static List<Question> list(Question... q) {
        List<Question> out = new ArrayList<>();
        for (Question x : q) out.add(x);
        return out;
    }

    public static List<Category> all() {
        return CATEGORIES;
    }

    public static String categoryName(Category c, String lang) {
        return "en".equals(lang) ? c.nameEn : c.nameFa;
    }

    public static String questionText(Question q, String lang) {
        return "en".equals(lang) ? q.en : q.fa;
    }
}
