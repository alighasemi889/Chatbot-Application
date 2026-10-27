package com.example.aichat;

import java.util.Arrays;
import java.util.List;

/**
 * Local, rule-based answers about IT topics. No external AI API is used.
 * Each entry matches if any of its keywords appears in the (normalized) question.
 * More specific entries come first.
 */
public class KnowledgeBase {

    private static class Entry {
        final List<String> keys;
        final String answer;

        Entry(String answer, String... keys) {
            this.keys = Arrays.asList(keys);
            this.answer = answer;
        }
    }

    private static final List<Entry> ENTRIES = Arrays.asList(
            new Entry(
                    "برنامه‌نویسی موبایل یعنی ساخت اپلیکیشن برای گوشی‌های هوشمند.\n\n" +
                    "• نیتیو (Native): با ابزار و زبان مخصوص هر سیستم‌عامل، مثلاً Kotlin/Java برای اندروید و Swift برای iOS. عملکرد و دسترسی به امکانات سخت‌افزاری بهتر است.\n" +
                    "• چندسکویی (Cross-platform): با یک کدبیس برای هر دو سیستم‌عامل، مثلاً Flutter، React Native. هزینه و زمان توسعه کمتر است.\n\n" +
                    "برای شروع اندروید، Kotlin و Android Studio گزینه‌ی رایج است.",
                    "برنامه‌نویسی موبایل", "برنامه نویسی موبایل", "اپ موبایل", "اندروید", "android", "ios", "flutter"),

            new Entry(
                    "امنیت اطلاعات یعنی حفاظت از داده‌ها و سیستم‌ها در برابر دسترسی یا تغییر غیرمجاز.\n\n" +
                    "مبنای آن سه اصل است (مثلث CIA):\n" +
                    "• محرمانگی (Confidentiality): فقط افراد مجاز داده را ببینند.\n" +
                    "• یکپارچگی (Integrity): داده بدون تغییر غیرمجاز بماند.\n" +
                    "• دسترس‌پذیری (Availability): سیستم در زمان لازم در دسترس باشد.\n\n" +
                    "موضوعات رایج: رمزنگاری، کنترل دسترسی، فایروال و آگاهی کاربران.",
                    "امنیت", "هک", "security", "حمله سایبری"),

            new Entry(
                    "سیسکو (Cisco) شرکتی آمریکایی و یکی از بزرگ‌ترین تولیدکنندگان تجهیزات شبکه است؛ از جمله روتر، سوئیچ و فایروال.\n\n" +
                    "در حوزه آموزش، مدرک‌های CCNA (پایه) و CCNP (پیشرفته) از معروف‌ترین گواهی‌های شبکه هستند که مفاهیمی مثل مسیریابی، سوئیچینگ و امنیت شبکه را پوشش می‌دهند.",
                    "سیسکو", "cisco", "ccna"),

            new Entry(
                    "مهندسی نرم‌افزار (Software Engineering) روشی نظام‌مند برای طراحی، ساخت، آزمون و نگهداری نرم‌افزار است.\n\n" +
                    "مراحل معمول چرخه‌ی حیات نرم‌افزار (SDLC): تحلیل نیازمندی‌ها، طراحی، پیاده‌سازی، آزمون، استقرار و نگهداری.\n\n" +
                    "رشته‌ی مهندسی نرم‌افزار بیشتر بر کیفیت، مدیریت پروژه، معماری و فرایند توسعه تمرکز دارد.",
                    "مهندسی نرم‌افزار", "مهندسی نرم افزار", "software engineering", "sdlc"),

            new Entry(
                    "مدل OSI هفت لایه دارد که توضیح می‌دهد داده‌ها در شبکه چطور منتقل می‌شوند:\n" +
                    "۱. فیزیکی  ۲. پیوند داده  ۳. شبکه  ۴. انتقال  ۵. نشست  ۶. ارائه  ۷. کاربردی.\n\n" +
                    "مثلاً IP در لایه‌ی شبکه و TCP/UDP در لایه‌ی انتقال کار می‌کنند.",
                    "مدل osi", "osi", "tcp/ip", "شبکه"),

            new Entry(
                    "فایروال (Firewall) دیواره‌ای امنیتی است که ترافیک ورودی و خروجی شبکه را بر اساس قوانین مشخص کنترل می‌کند.\n\n" +
                    "می‌تواند سخت‌افزاری (مثل تجهیزات سیسکو) یا نرم‌افزاری (مثل فایروال سیستم‌عامل) باشد.",
                    "فایروال", "firewall"),

            new Entry(
                    "رمزنگاری (Cryptography) علم محافظت از اطلاعات با تبدیل آن به شکلی است که فقط طرف مجاز بتواند بخواند.\n\n" +
                    "دو نوع اصلی: رمزنگاری متقارن (مثل AES، یک کلید برای رمز و رفع رمز) و نامتقارن (مثل RSA، کلید عمومی و خصوصی).",
                    "رمزنگاری", "رمز نگاری", "encryption", "cryptography"),

            new Entry(
                    "پایگاه داده مجموعه‌ای سازمان‌یافته از داده‌هاست که امکان ذخیره، جست‌وجو و مدیریت آن‌ها را فراهم می‌کند.\n\n" +
                    "دو دسته‌ی اصلی: رابطه‌ای (مثل MySQL و PostgreSQL، با SQL) و غیررابطه‌ای (مثل MongoDB).",
                    "پایگاه داده", "پایگاه داده", "دیتابیس", "database", "sql"),

            new Entry(
                    "برنامه‌نویسی یعنی نوشتن دستورهایی به زبان قابل فهم برای کامپیوتر تا یک مسئله را حل کند.\n\n" +
                    "زبان‌های رایج: Python برای شروع و داده، JavaScript برای وب، Java و Kotlin برای بک‌اند و اندروید. اگر بگویید به کدام حوزه علاقه دارید، مسیر یادگیری را پیشنهاد می‌دهم.",
                    "برنامه‌نویسی", "برنامه نویسی", "programming", "کدنویسی"),

            new Entry(
                    "این دستیار فعلاً بر اساس پایگاه دانش داخلی پاسخ می‌دهد و هنوز متن خلاصه نمی‌کند یا متن نمی‌نویسد.\n\n" +
                    "می‌توانید درباره‌ی موضوعات IT بپرسید، مثل: امنیت چیست، سیسکو چیست، برنامه‌نویسی موبایل چیست، مهندسی نرم‌افزار چیست.",
                    "خلاصه", "نوشتن متن", "مرحله", "مشکل را")
    );

    private static final String FALLBACK =
            "این سؤال در پایگاه دانش فعلی نیست. می‌توانید یکی از موضوعات زیر را بپرسید:\n" +
            "• امنیت چیست؟\n" +
            "• سیسکو چیست؟\n" +
            "• برنامه‌نویسی موبایل چیست؟\n" +
            "• مهندسی نرم‌افزار چیست؟\n" +
            "• فایروال، رمزنگاری، پایگاه داده، مدل OSI";

    public static String answer(String question) {
        String q = normalize(question);
        for (Entry e : ENTRIES) {
            for (String k : e.keys) {
                if (q.contains(normalize(k))) {
                    return e.answer;
                }
            }
        }
        return FALLBACK;
    }

    /** Normalizes Arabic letters to Persian, removes ZWNJ and punctuation, lowercases. */
    static String normalize(String s) {
        return s.toLowerCase()
                .replace('ي', 'ی')
                .replace('ك', 'ک')
                .replace("\u200c", "")
                .replace("‌", "")
                .replaceAll("[؟?!.،,:;()\"'«»]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
