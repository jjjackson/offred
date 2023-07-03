package com.zulu.offred;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private String currentSub = "main";
    private ViewPager viewPager;
    private ArrayList<String> subs = new ArrayList<>();

    private static final int ALARM_REQUEST_CODE = 123;
    private List<article> articles;
    private ArticlesPagerAdapter apa;
    private ViewPager vpa;

    private String[] colorlist = {"7f0000","191970","808000","008000","008b8b","7f007f","8fbc8f","b03060","ff4500","ffa500","00ff00","8a2be2","00ff7f","dc143c","00ffff","00bfff","0000ff","adff2f","da70d6","ff7f50","ff00ff","1e90ff","f0e68c","ffff54","90ee90","add8e6","ff1493","7b68ee","ffb6c1"};

    private ViewPager.OnPageChangeListener listener;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DatabaseHelper(getApplicationContext());
        scheduleAlarm();
        vpa = findViewById(R.id.articlesViewPager);
        articles = dbHelper.fetchArticles("subs");
        List<coms> cc = dbHelper.fetchComments("base");
        for (int i = 0; i < cc.size(); i++) {
            subs.add(cc.get(i).comment);
        }
        loadSub("subs");
        findViewById(R.id.add_button).setOnClickListener(view -> {
            dbHelper.insertsub(((TextView)findViewById(R.id.add_text_box)).getText().toString());
            findViewById(R.id.addModal).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.add_text_box)).setText("");
            loadSub("subs");
        });
        listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                System.out.println("selected?!"+position);
                if(position==0)currentSub="main";
                TextureVideoView tv = findViewById(1000000+position);
                if(tv!=null)tv.start();
                TextureVideoView tv2 = findViewById(1000000+position-1);
                if(tv2!=null)tv2.stopPlayback();
                TextureVideoView tv3 = findViewById(1000000+position+1);
                if(tv3!=null)tv3.stopPlayback();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        vpa.addOnPageChangeListener(listener);


    }
    private void loadSub(String sub){
        System.out.println("loading sub:"+sub);
        vpa = findViewById(R.id.articlesViewPager);
        List<coms> s = dbHelper.fetchComments(sub);
        if(s.size()==0 && !sub.equals("subs")){
            System.out.println("getting:"+sub);
            fetchSub all = new fetchSub();
            all.urls = "https://old.reddit.com/r/"+sub+"/";
            all.sub=sub;
            all.c = getApplicationContext();
            all.dbh = dbHelper;
            all.execute();
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadSub(sub);
                }
            }, 3000);
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadSub(sub);
                }
            }, 6000);
            dbHelper.startFetch(sub);
            //articles = dbHelper.fetchArticles("subs");
        }
        ArrayList<article> all = new ArrayList<>();

        all.addAll(articles);
        if(!sub.equals("subs")){
            all.add(new article(sub,sub,"Updating...","","",""));
        }else{

        }
        apa = new ArticlesPagerAdapter(this, all);
        vpa.setAdapter(apa);
        vpa.setCurrentItem(1);
    }
    private void loadarts(int num){
        System.out.println("loading arts:"+num);
        vpa = findViewById(R.id.articlesViewPager);

        List<article> a = dbHelper.fetchArticles(currentSub);
        ArrayList<article> all = new ArrayList<>();
        all.addAll(articles);
        all.add(new article(currentSub,currentSub,"Updated now","","",""));
        all.addAll(a);
        apa = new ArticlesPagerAdapter(this, all);
        vpa.setAdapter(apa);
        vpa.setCurrentItem(num+2);
    }

    public void closeZoom(View view) {
        findViewById(R.id.close).setVisibility(View.GONE);
        findViewById(R.id.bubble_image_zoom).setVisibility(View.GONE);
    }

    public static class article{
        public String _id="";
        public String title="";
        public String info="";
        //public List<coms> comments;

        public String link = "";

        public String media = "";
        public String sub = "";
        public article(String id, String t,String i, String l, String m, String s){
            _id=id;
            title=t;
            info=i;
            //comments=c;
            link = l;
            media = m;
            sub = s;
        }
    }

    public static class coms{
        public String comment="";
        public String data="";

        public String article="";
        public int indent=0;
         public String _id="";

        public coms(String id, String i,String d, String a,int s){
            _id=id;
            comment=i;
            data=d;
            article = a;
            indent=s;
        }

    }
    // Adapter for articlesViewPager
    public class ArticlesPagerAdapter extends PagerAdapter {

        private List<article> articles;
        private LayoutInflater inflater;

        public ArticlesPagerAdapter(Context context, List<article> articles) {
            this.articles = articles;
            inflater = LayoutInflater.from(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                articles.forEach(article -> System.out.println(article._id+","+article.title));
            }
        }

        @Override
        public int getCount() {
            return articles.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            View itemView = inflater.inflate(R.layout.articles, container, false);

            // Populate the itemView with article data
            article a = articles.get(position);
            if(currentSub.equals("main")){
                try {
                    List<coms> arts = dbHelper.fetchComments(subs.get(position));
                    TextView info = itemView.findViewById(R.id.info);
                    info.setText("downloaded:" + arts.size());
                }catch (Exception e){
                    e.printStackTrace();
                }

            }else{
                TextView info = itemView.findViewById(R.id.info);
                info.setText(a.info);
            }

            // Bind article data to the views in itemView
            TextView title = itemView.findViewById(R.id.title);
            title.setText(a.title);
            if(!a.media.isEmpty()) {

                System.out.println("loading media:"+a.media);
                if(a.media.endsWith(".mp4")){
                    ImageView media = itemView.findViewById(R.id.media);
                    TextureVideoView vv = new TextureVideoView(media.getContext());
                    vv.setId(1000000+position);
                    //VideoView vv = itemView.findViewById(R.id.vmedia);
                    vv.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    vv.setVideoPath(getApplicationContext().getFilesDir()+"/"+a.media);

                    ((LinearLayout) media.getParent()).addView(vv);
                    //vv.start();
//                    VideoView vw = itemView.findViewById(R.id.vmedia);
//                    vw.setVideoPath(getApplicationContext().getFilesDir()+"/"+a.media);
//                    vw.start();
                    System.out.println("started?"+a.media);
                }else {
                    ImageView media = itemView.findViewById(R.id.media);
                    media.setImageBitmap(loadImage(a.media));
                    media.setAdjustViewBounds(true);
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int screenWidth = displayMetrics.widthPixels;
                    media.setMinimumWidth(screenWidth);
                    media.setOnClickListener(view -> {
                        findViewById(R.id.close).setVisibility(View.VISIBLE);
                        findViewById(R.id.bubble_image_zoom).setVisibility(View.VISIBLE);
                        findViewById(R.id.bubble_image_zoom).setX(0);
                        findViewById(R.id.bubble_image_zoom).setTop(0);
                        ((MyImageView)findViewById(R.id.bubble_image_zoom)).setImageDrawable(((ImageView)view).getDrawable());
                    });
                    for (int i = 1; i < 10; i++) {
                        File file = new File(i + a.media);
                        if (file.exists()) {
                            ImageView nm = new ImageView(media.getContext());
                            nm.setImageBitmap(loadImage(i + a.media));
                            ((LinearLayout) media.getParent()).addView(nm);
                        } else {
                            i = 99;
                        }
                    }
                }
            }

            ImageButton lk = itemView.findViewById(R.id.imageButton);
            lk.setOnClickListener(view -> {
                if(a.title.equals("Subreddits")){
                    System.out.println("download all?");
                    dbHelper.clearDB();
                    for (String sub : subs) {
                        loadSub(sub);
                    }
                    loadSub("subs");
                }
                if(a.link.length()>0){
                    if(a.link.substring(0,3).equals("/r/"))a.link="https://old.reddit.com"+a.link;
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(a.link)));

                }
            });

            if(a.link.length()>0)lk.setRotation(-90);


            container.addView(itemView);
            RecyclerView coms = itemView.findViewById(R.id.commentsRecyclerView);
            System.out.println("fetching coms for art:"+a._id);
            List acoms = dbHelper.fetchComments(a._id);
            System.out.println("found:"+acoms.size());

            CommentsAdapter  commentsAdapter = new CommentsAdapter( acoms);

            coms.setAdapter(commentsAdapter);
            coms.setLayoutManager(new LinearLayoutManager(container.getContext()));
            ((ScrollView)itemView.findViewById(R.id.scrolling)).smoothScrollTo(0, 0);
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((ScrollView)itemView.findViewById(R.id.scrolling)).smoothScrollTo(0, 0);
                }
            }, 300);
            return itemView;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

        private List<coms> comments;
        private List<coms> ocomments;

        public CommentsAdapter(List<coms> comments) {
            this.comments = comments;
            ocomments = new ArrayList<>();
            ocomments.addAll(comments);
            if(comments.size()>0&&comments.get(0).article.equals("base"))this.comments.add(comments.size(),new coms("addme","add","","base",0));
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.com, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            coms comment = comments.get(position);
            // Bind comment data to the views in the ViewHolder
            //System.out.println("dd"+comment);
            holder.bind(comment);
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        public class CommentViewHolder extends RecyclerView.ViewHolder {
            // Declare your views here
            TextView titles;
            TextView info;
            int prev;
            int hidei = 99;
            public CommentViewHolder(View itemView) {
                super(itemView);
                // Initialize your views here
                titles = itemView.findViewById(R.id.comtex);
                info = itemView.findViewById(R.id.comdata);
                // You can apply indentation to the views as needed
                // For example, you can set the left margin of a TextView
                // based on the indentation level of the comment.
                Random random = new Random();
                prev=Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            }

            public void bind(coms comment) {
                // Bind comment data to the views here
                //System.out.println("ee"+comment._id);
                if(comment.indent>hidei)return;
                hidei=99;
                titles.setText(comment.comment);
                titles.setTag(comment.indent);
                info.setText(comment.data);
                info.setVisibility(comment.data.equals("")?View.GONE:View.VISIBLE);
                info.setTag(comment);
                LinearLayout l = (LinearLayout) info.getParent().getParent();

                l.findViewById(R.id.preSpacer).setMinimumWidth(comment.indent>1?(10*(comment.indent-1)):0);
                if(comment.article.equals("base")){
                    if(comment._id.equals("addme")){
                        itemView.setOnClickListener(view -> findViewById(R.id.addModal).setVisibility(View.VISIBLE));
                        itemView.findViewById(R.id.addsub).setOnClickListener(view -> findViewById(R.id.addModal).setVisibility(View.VISIBLE));
                        itemView.findViewById(R.id.addsub).setVisibility(View.VISIBLE);
                    }else {
                        List<coms> arts = dbHelper.fetchComments(comment.comment);
                        info.setText("downloaded:" + arts.size());
                        itemView.setOnClickListener(view -> {
                            currentSub = comment.comment;
                            loadSub(comment.comment);
                            System.out.println("change current sub to " + currentSub);
                        });
                        itemView.findViewById(R.id.dlsub).setVisibility(View.VISIBLE);
                        itemView.findViewById(R.id.removesub).setVisibility(View.VISIBLE);
                        itemView.findViewById(R.id.removesub).setOnClickListener(view -> {
                            dbHelper.removeSub(comment.comment);
                            loadSub("subs");
                        });
                        itemView.findViewById(R.id.dlsub).setOnClickListener(view -> {
                            System.out.println("downlading sub" + comment.comment);
                            dbHelper.clearSub(comment.comment);
                            loadSub(comment.comment);
                        });
                    }

                }else{
                    itemView.setOnClickListener(view -> {
                        for (int i = 0; i < subs.size(); i++) {
                            if(subs.get(i).equals(comment.article)){
                                System.out.println( "loading:"+comment._id);
                                loadarts(this.getAdapterPosition());
                                return;
                            }
                        }
                        if(titles.getText().equals("+")){
                            try {
                                System.out.println("unhiding!");
                                int found = -1;
                                for (int i = 0; i < ocomments.size(); i++) {
                                    if (ocomments.get(i)._id.equals(comment._id)) {
                                        int cc = comments.indexOf(comment);
                                        System.out.println("unhiding:" + cc);
                                        comments.remove(cc);
                                        comments.add(cc, ocomments.get(i));
                                        System.out.println("adding:" + ocomments.get(i).comment);
                                        found = ocomments.get(i).indent;
                                    } else {
                                        if (found >= 0) {
                                            if (ocomments.get(i).indent > found) {
                                                comments.add(i, ocomments.get(i));
                                            } else {
                                                found = -1;
                                            }
                                        }
                                    }
                                }

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            RecyclerView par = (RecyclerView) view.getParent();
                            par.getAdapter().notifyDataSetChanged();
                        }else {
                            info.setText("");
                            titles.setText("+");
                            try {
                                int found = -1;
                                for (int i = 0; i < ocomments.size(); i++) {
                                    if (ocomments.get(i).equals(comment)) {

                                        comments.add(comments.indexOf(comment), new coms(comment._id, "+", "", comment.article, comment.indent));
                                        comments.remove(comment);
                                        found = ocomments.get(i).indent;
                                        System.out.println("removing comment with indent:" + found);
                                    } else {
                                        if (found >= 0) {
                                            System.out.println("comment:" + ocomments.get(i).indent);
                                            if (ocomments.get(i).indent > found) {
                                                comments.remove(ocomments.get(i));
                                                System.out.println("removed!");
                                            } else {
                                                System.out.println("kept!");
                                                found = -1;
                                            }
                                        }
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            RecyclerView par = (RecyclerView) view.getParent();
                            par.getAdapter().notifyDataSetChanged();
                        }
                    });
                }
                if(comment.indent>0){
                    l.findViewById(R.id.comSpacer).setMinimumWidth((10));
//                    Random random = new Random();
//                    int color = 1;
//                    do {
//                        color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
//                    } while (isColorTooSimilar(color));
//                    prev=color;
                    l.findViewById(R.id.comSpacer).setBackgroundColor(Color.parseColor("#"+colorlist[comment.indent-1]));
                }

                l.setLeft(comment.indent);
            }


        private boolean isColorTooSimilar(int color) {
            int deltaThreshold = 100; // Adjust this threshold based on your preference

            int redDiff = Math.abs(Color.red(color) - Color.red(prev));
            int greenDiff = Math.abs(Color.green(color) - Color.green(prev));
            int blueDiff = Math.abs(Color.blue(color) - Color.blue(prev));

            int totalDiff = redDiff + greenDiff + blueDiff;

            return totalDiff < deltaThreshold;
        }
        }
    }
    public static class fetchSub extends AsyncTask<Void, Void, String> {

        public String urls="";
        public String sub = "";

        public DatabaseHelper dbh;

        public Context c;

        public Runnable after;

        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(urls);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                    bufferedReader.close();
                } else {
                    result = "Error: " + responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if(dbh==null)dbh=new DatabaseHelper(c);
                //List<coms> ar = new ArrayList<>();
                //System.out.println(result);
                int artCount = 1;
                String[] ress = result.split("data-type=\"link\"");
                for (int i = 1; i < ress.length; i++) {
                    //System.out.println(indent);
                    //System.out.println(ress[i]);
                    if(ress[i].indexOf("data-adserver-imp-pixel")>0)continue;
                    //System.out.println(ress[i]);
                    int subrs = ress[i].indexOf("data-subreddit=\"")+16;
                    String subr = ress[i].substring(subrs,ress[i].indexOf("\"",subrs));
                    int medias = ress[i].indexOf("data-url=\"")+10;
                    String media = ress[i].substring(medias,ress[i].indexOf("\"",medias));
                    int link = ress[i].indexOf("data-event-action=\"title\"")+32;
                    String links = ress[i].substring(link,ress[i].indexOf("\"",link));
                    //System.out.println("links:"+links);
                    String title = ress[i].substring(ress[i].indexOf(">",link)+1,ress[i].indexOf("<",link));
                    //System.out.println("title:"+title);
                    int auths = ress[i].indexOf("data-author=\"")+13;
                    int authe = ress[i].indexOf("\"",auths);
                    String author = ress[i].substring(auths,authe);
                    int dates = ress[i].indexOf("live-timestamp")+16;
                    int datee = ress[i].indexOf("</time",dates);
                    String date = ress[i].substring(dates,datee);
                    //ar.add(new coms(title,author+", "+date,"",0));
                    int comsl = ress[i].indexOf("data-permalink=")+16;
                    int comsle = ress[i].indexOf("\"",comsl);
                    String commsl = ress[i].substring(comsl,comsle);
                    //System.out.println("https://old.reddit.com"+commsl);
                    String[] aids = commsl.split("/");
                    String aid = aids[aids.length-2];
                    //System.out.println("aid:"+aid);
                    fetchArticle comA = new fetchArticle();
                    comA.urls = "https://old.reddit.com"+commsl;
                    comA.aid=aid;
                    comA.dbh = dbh;

                    //System.out.println(comA.urls);
                    comA.execute();
                    dbh.insertComment(sub+artCount,sub,subr+","+author+","+date,title,0);
                    artCount++;

                    String m = "";
                    if(media.startsWith("https://v.redd.it/")){
                        String rvdl = "https://www.rvdl.com"+commsl;
                        Thread thread = new Thread(() -> getVideoFromURL(rvdl, aid,c));
                        thread.start();
                        m = aid+".mp4";
                    }else{
                        m = downloadLink(media,aid,c);
                    }
                    dbh.insertArticle(aid,title,author+","+date,links,m,sub);

                }
                //loadSub(sub);
                //String[] subs = urls.split("/");


//                //System.out.println(result);
//                for (int i = 0; i < articles.size(); i++) {
//                    if(articles.get(i).title.equals(subs[subs.length-1])){
//                        articles.remove(i);
//                        i--;
//                    }
//                }
//                articles.add(new article("",subs[subs.length-1],"Last Updated:Never","downloadArticle","", ""));
//                vpa.setAdapter(apa);

            } else {
                //textView.setText("Error fetching website content.");
            }
        }
    }

    private static String downloadLink(String l, String name,Context c) {
        System.out.println(l.substring(0, 18));
        String m = "";
        if (l.startsWith("https://i.redd.it/") || l.startsWith("https://i.imgur.com/") || l.startsWith("https://imgur.com/") || l.startsWith("https://imgur.io/")) {
            m = name + ".jpg";
            Thread thread = new Thread(() -> saveBitmap(getBitmapFromURL(l), name+ ".jpg",c));
            thread.start();
        }
        if(l.startsWith("https://www.reddit.com/gallery/")){
            String finalM = m;
            m = name+".jpg";
            Thread g = new Thread(() ->{ getURLSFromGallery(l, finalM,c); });
            g.start();
        }
        if (l.startsWith("https://v.redd.it/")){
            m = name + ".mp4";
            Thread thread = new Thread(() -> getVideoFromURL(l+"/DASH_480.mp4", name,c));
            thread.start();
        }
        return m;
    }

    public static class fetchArticle extends AsyncTask<Void, Void, String> {

        public String urls="";
        public String aid = "";

        public DatabaseHelper dbh;

        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            HttpURLConnection urlConnection = null;

            try {
                System.out.println("fetching:"+urls);
                URL url = new URL(urls);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }

                    bufferedReader.close();
                } else {
                    result = "Error: " + responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                // Set the first word as the text in the TextView
                List<coms> ar = new ArrayList<>();
                //ar.add(new coms("test",result,0));

                //System.out.println(result);
                String[] ress = result.split("data-type=\"comment\"");
                int indent = 0;
                List<String> ids = new ArrayList();
                List<Integer> idi = new ArrayList();
                int link = result.indexOf("data-event-action=\"title\"")+32;
                String links = result.substring(link,result.indexOf("\"",link));
                //System.out.println("links:"+links);
                String title = result.substring(result.indexOf(">",link)+1,result.indexOf("<",link));
                //System.out.println("title:"+title);
                for (int i = 1; i < ress.length; i++) {
                    //System.out.println(indent);
                    //System.out.println(ress[i]);
                    int auths = ress[i].indexOf("data-author=\"")+13;
                    int authe = ress[i].indexOf("\"",auths);
                    String author = ress[i].substring(auths,authe);
                    int dates = ress[i].indexOf("live-timestamp")+16;
                    int datee = ress[i].indexOf("</time",dates);
                    String date = ress[i].substring(dates,datee);
                    int txts = ress[i].indexOf("usertext-body");
                    txts = ress[i].indexOf(">",txts)+1;
                    int txte = ress[i].indexOf("</div>",txts);
                    String text = ress[i].substring(txts,txte);
                    int id = ress[i].indexOf("class=\"parent\"")+24;
                    String cid = ress[i].substring(id,id+7);
                    int parent = ress[i].indexOf("data-event-action=\"parent\"")-9;
                    if(parent<0)parent=id;
                    String pid = ress[i].substring(parent,parent+7);
                    indent=0;
                    if(ids.indexOf(pid)!=-1) indent = idi.get(ids.indexOf(pid))+1;
                    ids.add(cid);
                    idi.add(indent);
                    String text2 = "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        text2 = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT).toString();
                    } else {
                        text2 = Html.fromHtml(text).toString();
                    }
                    //System.out.println(cid+","+pid+","+indent+","+text2);
                    dbh.insertComment(aid+i,aid,author+", "+date,text2,indent);
                    //ar.add(new coms(author+", "+date,text2,"",indent));
                }

                //System.out.println(result);
                //articles.add(new article("", title,links,links,"", ""));
                //vpa.setAdapter(apa);
            } else {
                //textView.setText("Error fetching website content.");
            }
        }
    }



    private static void saveBitmap(Bitmap b, String name,Context c){
        FileOutputStream fos = null;
        try {
            File file = new File(c.getFilesDir(), name);
            fos = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    // Close the output stream
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private Bitmap loadImage(String name){
        String filename = name;
        FileInputStream fis = null;
        Bitmap b = null;
        try {
            fis = getApplicationContext().openFileInput(filename);
            b = BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return b;
    }
    public static Bitmap getBitmapFromURL(String src) {

        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String getVideoFromURL(String src, String name, Context c) {

        try {
            System.out.println("Downloading video:"+src);
            System.out.println("Downloading video:"+name);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            File outputDir = c.getFilesDir();
            //File outputFile = File.createTempFile(name, ".mp4", outputDir);
            File outputFile = new File(c.getFilesDir(), name+".mp4");

            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            fileOutputStream.close();
            inputStream.close();
            return name+".mp4";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String[] getURLSFromGallery(String g, String m, Context c){

            String result = "";
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(g);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                    bufferedReader.close();
                } else {
                    result = "Error: " + responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            String [] ss = {};
            ArrayList<String> sd = new ArrayList();
        for (String s : result.split("https://preview.redd.it/")) {
            if(s.charAt(17)=='?'){
                String u = "https://i.redd.it/"+s.substring(0,17);
                if( !sd.contains(u)) sd.add(u);
            }
        }
        for (int i = 0; i < sd.size(); i++) {
            saveBitmap(getBitmapFromURL(sd.get(i)),(i>0?i:"")+m+".jpg",c);
        }



            return ss;

    }
    @Override
    public void onBackPressed() {
        if(currentSub.equals("main")) super.onBackPressed();
        vpa.setCurrentItem(0);
    }

    private void scheduleAlarm() {
        // Set the desired time for the alarm (6 AM)
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Create an intent for the BroadcastReceiver
        Intent intent = new Intent(this, AlarmReceiver.class);

        // Create a PendingIntent to be triggered when the alarm fires
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);

        // Get the AlarmManager service and set the alarm
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }



}