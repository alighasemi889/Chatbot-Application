package com.example.aichat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Offline knowledge base for cybersecurity, programming and artificial intelligence.
 * Questions are matched against keywords; if nothing matches, the chat falls back to the API.
 */
public final class KnowledgeBase {

    public static final class Entry {
        public final String title;
        public final String[] keywords;
        public final String answer;

        Entry(String title, String[] keywords, String answer) {
            this.title = title;
            this.keywords = keywords;
            this.answer = answer;
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private static void add(String title, String[] keywords, String answer) {
        ENTRIES.add(new Entry(title, keywords, answer));
    }

    static {
        // ---- Cybersecurity ----
        add("فیشینگ (Phishing)",
                new String[]{"فیشینگ", "phishing", "ایمیل جعلی", "کلاهبرداری اینترنتی"},
                "فیشینگ نوعی حمله مهندسی اجتماعی است که در آن مهاجم با ایمیل، پیامک یا صفحه جعلی خود را شبیه سرویس معتبر جا می‌زند تا رمز عبور یا اطلاعات بانکی شما را بگیرد.\n\n" +
                "برای جلوگیری: نشانی فرستنده و دامنه لینک را بررسی کنید، به درخواست‌های فوری برای رمز یا کد یک‌بار مصرف پاسخ ندهید و در صورت شک مستقیم از طریق اپ رسمی وارد شوید.");

        add("احراز هویت دو مرحله‌ای (2FA)",
                new String[]{"دو مرحله", "2fa", "two factor", "تایید دو مرحله", "otp", "احراز هویت"},
                "احراز هویت دو مرحله‌ای یعنی علاوه بر رمز عبور، مرحله دوم دیگری (مثل کد یک‌بار مصرف، اپ احراز هویت یا کلید سخت‌افزاری) برای ورود لازم باشد.\n\n" +
                "حتی اگر رمز شما لو برود، مهاجم بدون مرحله دوم نمی‌تواند وارد حساب شود. کدهای پیامکی امن‌ترین گزینه نیستند؛ اپ‌های TOTP مثل Google Authenticator امن‌تر هستند.");

        add("رمز عبور قوی",
                new String[]{"رمز قوی", "رمز عبور", "پسورد", "password", "رمز امن"},
                "یک رمز عبور قوی طولانی (حداقل ۱۲ کاراکتر)، ترکیبی از حروف بزرگ و کوچک، عدد و نماد باشد و در سایت‌های مختلف تکراری نباشد.\n\n" +
                "استفاده از یک مدیر رمز (Password Manager) کمک می‌کند برای هر سرویس رمز منحصربه‌فرد داشته باشید.");

        add("SQL Injection",
                new String[]{"sql injection", "sql", "تزریق sql", "تزریق اس کیو ال"},
                "SQL Injection زمانی رخ می‌دهد که ورودی کاربر بدون اعتبارسنجی مستقیم داخل کوئری پایگاه داده قرار گیرد و مهاجم بتواند دستورهای SQL دلخواه اجرا کند.\n\n" +
                "راه حل اصلی استفاده از Prepared Statements یا Parameterized Queries است؛ همچنین اعتبارسنجی ورودی و حداقل دسترسی برای کاربر دیتابیس ضروری است.");

        add("XSS (اسکریپت‌نویسی بین‌سایتی)",
                new String[]{"xss", "cross site", "اسکریپت بین سایتی"},
                "در حمله XSS مهاجم کد جاوااسکریپت مخرب را در صفحه‌ای که دیگران می‌بینند تزریق می‌کند تا کوکی یا اطلاعات کاربر را بدزدد.\n\n" +
                "پیشگیری: خروجی‌ها را escape کنید، از Content-Security-Policy استفاده کنید و محتوای ناامن را مستقیم در innerHTML قرار ندهید.");

        add("بدافزار و باج‌افزار",
                new String[]{"بدافزار", "ویروس", "باج افزار", "ransomware", "malware", "ransom"},
                "بدافزار نرم‌افزار مخربی است که برای آسیب یا سرقت اطلاعات ساخته شده است. باج‌افزار (Ransomware) فایل‌ها را رمز می‌کند و برای بازگشایی باج می‌خواهد.\n\n" +
                "مهم‌ترین دفاع، پشتیبان‌گیری منظم و جدا از سیستم اصلی است؛ همچنین به‌روزرسانی سیستم و پرهیز از باز کردن پیوست‌های ناشناس کمک زیادی می‌کند.");

        add("فایروال و VPN",
                new String[]{"فایروال", "firewall", "vpn", "وی پی ان"},
                "فایروال ترافیک ورودی و خروجی شبکه را طبق قوانین فیلتر می‌کند. VPN ارتباط شما را رمزنگاری کرده و از طریق سرور میانی عبور می‌دهد تا هویت و مسیر اینترنتی‌تان پنهان‌تر شود.\n\n" +
                "VPN جایگزین امنیت کامل نیست؛ برای امنیت بیشتر هم‌زمان به‌روزرسانی، آنتی‌ویروس و احراز هویت دو مرحله‌ای لازم است.");

        add("مهندسی اجتماعی",
                new String[]{"مهندسی اجتماعی", "social engineering"},
                "مهندسی اجتماعی یعنی فریب انسان برای دور زدن امنیت فنی؛ مثلاً تماس تلفنی با وانمود به پشتیبانی یا ایجاد فشار زمانی برای گرفتن اطلاعات.\n\n" +
                "قاعده طلایی: هیچ سازمان معتبری رمز یا کد تأیید شما را نمی‌خواهد. درخواست‌های فوری و غیرعادی را همیشه از کانال رسمی تأیید کنید.");

        // ---- Programming ----
        add("تفاوت == و equals در جاوا",
                new String[]{"== و equals", "equals", "تفاوت ==", "مقایسه رشته", "string compare"},
                "در جاوا == برای انواع اولیه مقدار را مقایسه می‌کند، اما برای اشیا بررسی می‌کند که آیا هر دو به یک شیء در حافظه اشاره می‌کنند.\n\n" +
                "برای مقایسه محتوای رشته‌ها و اشیا از equals استفاده کنید، مثلاً str1.equals(str2).");

        add("Activity Lifecycle در اندروید",
                new String[]{"lifecycle", "چرخه حیات", "oncreate", "activity lifecycle", "onresume"},
                "هر Activity در اندروید از مراحلی مثل onCreate، onStart، onResume، onPause، onStop و onDestroy عبور می‌کند.\n\n" +
                "داده‌های موقت را در onPause یا onStop ذخیره کنید، منابع سنگین را در onStop آزاد کنید و کارهای شروع را در onCreate یا onStart انجام دهید.");

        add("REST API",
                new String[]{"rest", "api", "restful", "http method", "get post put delete"},
                "REST یک سبک طراحی API مبتنی بر HTTP است که منابع را با آدرس (URL) مشخص می‌کند و از متدهای GET، POST، PUT، PATCH و DELETE استفاده می‌کند.\n\n" +
                "پاسخ‌ها معمولاً در JSON هستند و کدهای وضعیت مثل 200، 201، 400، 401 و 404 معنای مشخصی دارند.");

        add("Git و Commit",
                new String[]{"git", "commit", "branch", "گیت", "merge", "rebase"},
                "Git سیستم کنترل نسخه است. هر commit یک تصویر ثبت‌شده از تغییرات است و branch مسیر موازی توسعه را نگه می‌دارد.\n\n" +
                "دستورهای پرکاربرد: git status، git add، git commit، git push و git pull. برای پیام commit بهتر است کوتاه و گویا باشد.");

        add("Big-O و پیچیدگی الگوریتم",
                new String[]{"big o", "bigo", "پیچیدگی", "complexity", "الگوریتم", "زمان اجرا"},
                "Big-O نشان می‌دهد که زمان یا حافظه یک الگوریتم با افزایش ورودی چگونه رشد می‌کند. O(1) ثابت، O(log n) لگاریتمی، O(n) خطی و O(n²) درجه دوم است.\n\n" +
                "هدف انتخاب الگوریتمی است که با بزرگ‌تر شدن داده‌ها هنوز سریع بماند؛ مثلاً جستجوی دودویی در لیست مرتب از O(log n) است.");

        add("Async و Coroutine",
                new String[]{"async", "coroutine", "همزمان", "thread", "نخ", "concurrency"},
                "کارهای سنگین مثل شبکه یا خواندن فایل نباید روی Thread اصلی انجام شوند تا برنامه قفل نشود. در اندروید از Coroutine یا Executor برای اجرای پس‌زمینه استفاده می‌شود.\n\n" +
                "نتیجه را به Thread اصلی برگردانید تا رابط کاربری به‌روز شود.");

        add("اصل‌های SOLID",
                new String[]{"solid", "اصول طراحی", "single responsibility", "وابستگی"},
                "SOLID پنج اصل طراحی شی‌گرا است: تک‌مسئولیتی، باز-بسته، جایگزینی لیسکوف، جداسازی رابط‌ها و وارونگی وابستگی.\n\n" +
                "هدف این است که کدها قابل تغییر، تست و نگهداری باشند.");

        // ---- Artificial intelligence ----
        add("یادگیری ماشین و یادگیری عمیق",
                new String[]{"یادگیری ماشین", "machine learning", "یادگیری عمیق", "deep learning", "شبکه عصبی", "neural"},
                "یادگیری ماشین الگوریتم‌هایی هستند که از داده یاد می‌گیرند بدون اینکه قانون‌ها را صریح برنامه‌نویسی کنیم. یادگیری عمیق زیرمجموعه‌ای از آن است که با شبکه‌های عصبی چندلایه کار می‌کند.\n\n" +
                "یادگیری عمیق برای تصویر، صدا و متن قدرتمند است، اما به داده و توان محاسباتی بیشتری نیاز دارد.");

        add("Overfitting (بیش‌برازش)",
                new String[]{"overfitting", "بیش برازش", "بیش‌برازش", "underfitting"},
                "Overfitting زمانی رخ می‌دهد که مدل داده آموزش را حفظ می‌کند ولی روی داده جدید عملکرد ضعیفی دارد. عکس آن Underfitting است که مدل حتی آموزش را هم یاد نمی‌گیرد.\n\n" +
                "راه‌کارها: داده بیشتر، Regularization، Dropout و اعتبارسنجی روی داده جدا.");

        add("LLM و Token",
                new String[]{"llm", "مدل زبانی", "توکن", "token", "large language model"},
                "مدل زبانی بزرگ (LLM) مدلی است که با پیش‌بینی توکن بعدی متن تولید می‌کند. توکن واحدی کوچک‌تر از کلمه است (گاهی بخشی از یک کلمه یا یک نماد).\n\n" +
                "طول ورودی و خروجی بر حسب توکن محدود است و هزینه استفاده هم معمولاً بر اساس تعداد توکن محاسبه می‌شود.");

        add("Prompt Engineering",
                new String[]{"prompt", "پرامپت", "مهندسی پرامپت", "prompt engineering"},
                "مهندسی پرامپت یعنی نوشتن دستور دقیق و ساختارمند برای مدل. نقش، هدف، قالب خروجی و مثال کمک می‌کند پاسخ مرتبط‌تر باشد.\n\n" +
                "مثال: «به عنوان یک مهندس امنیت، سه ریسک اصلی این API را فهرست کن و برای هر کدام راه‌حل بده.»");

        add("Embedding و RAG",
                new String[]{"embedding", "امبدینگ", "rag", "بازیابی", "retrieval", "vector"},
                "Embedding تبدیل متن به بردار عددی است که معنای آن را نگه می‌دارد؛ متن‌های مشابه بردارهای نزدیک دارند.\n\n" +
                "RAG (Retrieval-Augmented Generation) ابتدا مرتبط‌ترین اسناد را با embedding پیدا می‌کند و سپس آن‌ها را به مدل می‌دهد تا پاسخ مستند بسازد.");

        add("Gradient Descent",
                new String[]{"gradient", "گرادیان", "گرادیان نزولی", "backpropagation", "پس انتشار"},
                "Gradient Descent روشی برای کمینه کردن خطای مدل است: در هر گام وزن‌ها را در جهت کاهش خطا و به اندازه نرخ یادگیری (learning rate) تغییر می‌دهیم.\n\n" +
                "Backpropagation گرادیان‌ها را در شبکه عصبی محاسبه می‌کند.");
    }

    private KnowledgeBase() {}

    /** Returns the best matching entry, or null when the question is outside the knowledge base. */
    public static Entry find(String question) {
        String q = normalize(question);
        if (q.isEmpty()) return null;
        Entry best = null;
        int bestScore = 0;
        for (Entry e : ENTRIES) {
            int score = 0;
            for (String k : e.keywords) {
                String nk = normalize(k);
                if (!nk.isEmpty() && q.contains(nk)) {
                    score += nk.length() >= 4 ? 2 : 1;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = e;
            }
        }
        return bestScore >= 2 ? best : null;
    }

    /** Formats an entry for display in the chat. */
    public static String format(Entry e) {
        return "📚 " + e.title + "\n\n" + e.answer;
    }

    static String normalize(String s) {
        String t = s.toLowerCase(Locale.ROOT)
                .replace('ي', 'ی')
                .replace('ك', 'ک')
                .replace('\u200c', ' ')
                .replace('\u200b', ' ')
                .replaceAll("[\\p{Punct}؟،؛«»]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return t;
    }
}
