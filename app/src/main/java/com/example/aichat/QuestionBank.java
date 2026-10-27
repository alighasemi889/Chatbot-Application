package com.example.aichat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local multiple-choice question bank for IT quizzes. */
public class QuestionBank {

    public static class Question {
        public final String text;
        public final String[] options;
        public final int correct;

        Question(String text, int correct, String... options) {
            this.text = text;
            this.options = options;
            this.correct = correct;
        }
    }

    private static final List<Question> ALL = new ArrayList<>();

    static {
        ALL.add(new Question("کدام مورد یکی از سه اصل مثلث CIA نیست؟", 3,
                "محرمانگی", "یکپارچگی", "دسترس‌پذیری", "سرعت پردازش"));
        ALL.add(new Question("رمزنگاری نامتقارن از چند کلید استفاده می‌کند؟", 1,
                "یک کلید", "دو کلید", "سه کلید", "بدون کلید"));
        ALL.add(new Question("کدام الگوریتم معروف رمزنگاری متقارن است؟", 1,
                "RSA", "AES", "DSA", "ElGamal"));
        ALL.add(new Question("فایروال عمدتاً برای چه کاری استفاده می‌شود؟", 0,
                "کنترل ترافیک شبکه بر اساس قوانین", "ویرایش تصاویر", "ذخیره ویدیو", "افزایش سرعت پردازنده"));
        ALL.add(new Question("مدل OSI چند لایه دارد؟", 2,
                "۴", "۵", "۷", "۹"));
        ALL.add(new Question("کدام لایه‌ی OSI مسئول مسیریابی بسته‌ها است؟", 1,
                "لایه ۲ (Data Link)", "لایه ۳ (Network)", "لایه ۴ (Transport)", "لایه ۷ (Application)"));
        ALL.add(new Question("کدام پروتکل در لایه‌ی انتقال کار می‌کند؟", 1,
                "IP", "TCP", "HTTP", "Ethernet"));
        ALL.add(new Question("SQL برای چه کاری استفاده می‌شود؟", 1,
                "طراحی گرافیک", "کار با پایگاه داده‌ی رابطه‌ای", "کامپایل کد", "مدیریت حافظه"));
        ALL.add(new Question("کدام یک پایگاه داده‌ی غیررابطه‌ای است؟", 2,
                "MySQL", "PostgreSQL", "MongoDB", "SQLite"));
        ALL.add(new Question("SDLC در مهندسی نرم‌افزار مخفف چیست؟", 0,
                "چرخه‌ی حیات توسعه‌ی نرم‌افزار", "سیستم مدیریت داده", "زبان طراحی ساختار", "پروتکل ارتباط سرور"));
        ALL.add(new Question("کدام زبان برای توسعه‌ی اصلی (Native) اندروید رایج است؟", 0,
                "Kotlin", "Swift", "COBOL", "Fortran"));
        ALL.add(new Question("Swift زبان اصلی توسعه برای کدام سیستم‌عامل است؟", 1,
                "اندروید", "iOS", "ویندوز", "لینوکس"));
        ALL.add(new Question("Docker برای چه کاری به‌کار می‌رود؟", 0,
                "اجرای برنامه در کانتینر", "طراحی پایگاه داده", "ساخت صفحه‌ی وب", "ویرایش ویدیو"));
        ALL.add(new Question("CI/CD بیشتر برای چه کاری است؟", 0,
                "خودکارسازی ساخت، آزمون و استقرار", "ساخت لوگو", "ویرایش متن", "مدیریت رمز عبور"));
        ALL.add(new Question("شرکت Cisco تولیدکننده‌ی چه نوع تجهیزاتی است؟", 1,
                "خودرو", "شبکه", "لوازم خانگی", "بازی رایانه‌ای"));
        ALL.add(new Question("مدرک CCNA معمولاً در کدام سطح است؟", 0,
                "پایه", "پیشرفته", "دکترا", "مدیریتی"));
        ALL.add(new Question("یادگیری ماشین زیرشاخه‌ی کدام حوزه است؟", 0,
                "هوش مصنوعی", "گرافیک کامپیوتری", "طراحی صفحه", "مدیریت شبکه"));
        ALL.add(new Question("Python بیشتر برای چه کاری شناخته شده است؟", 1,
                "فقط بازی‌های سه‌بعدی", "شروع برنامه‌نویسی و تحلیل داده", "ساخت سخت‌افزار", "کنترل ترافیک هوایی"));
        ALL.add(new Question("تست نفوذ مجاز چگونه انجام می‌شود؟", 0,
                "با اجازه‌ی صاحب سیستم", "بدون اطلاع صاحب سیستم", "فقط روی سیستم‌های عمومی", "هیچ‌وقت قانونی نیست"));
        ALL.add(new Question("یکی از نشانه‌های رایج فیشینگ چیست؟", 0,
                "ایمیل با درخواست فوری برای ورود یا رمز", "نصب به‌روزرسانی از منبع رسمی",
                "اجرای آنتی‌ویروس", "تغییر دوره‌ای رمز عبور"));
    }

    /** Returns up to count random questions (shuffled). */
    public static List<Question> pick(int count) {
        List<Question> copy = new ArrayList<>(ALL);
        Collections.shuffle(copy);
        return new ArrayList<>(copy.subList(0, Math.min(count, copy.size())));
    }
}
