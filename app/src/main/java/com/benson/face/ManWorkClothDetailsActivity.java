package com.benson.face;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ManWorkClothDetailsActivity extends AppCompatActivity {
    private String scene;
    private int index;
    private int[] arr;
    private String style;
    private GestureDetector gue;
    private MyApplication myApplication;

    private TextView descriptionText;
    private ImageView shoesImg;
    private ImageView necktieImg;
    private ImageView wristWatchImg;
    private ImageView briefCaseImg;
    private ImageView refreshImg;

    private ViewPager mViewPager;
    private List<View> mViews;
    private ViewGroup mDotViewGroup;
    private List<ImageView> mDotViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.man_work_clothing);
        Toast.makeText(ManWorkClothDetailsActivity.this, "左滑可返回主界面", Toast.LENGTH_SHORT).show();
        initView();
        index = getIntent().getIntExtra("index", 0);
        arr = getIntent().getIntArrayExtra("arr");
        style = getIntent().getStringExtra("style");
        scene = getIntent().getStringExtra("scene");

        setViewPager();
        //动态设置其他图片
        String shoes = "shoes";
        shoesImg.setImageBitmap(getOtherImg(shoes));
        String necktie = "necktie";
        necktieImg.setImageBitmap(getOtherImg(necktie));
        String wristwatch = "wrist_watch";
        wristWatchImg.setImageBitmap(getOtherImg(wristwatch));
        String briefcase = "brief_case";
        briefCaseImg.setImageBitmap(getOtherImg(briefcase));
        descriptionText.setText(getDescription());//获取strings.xml里的服装描述文本信息
        gue = new GestureDetector(ManWorkClothDetailsActivity.this, new ManWorkClothDetailsActivity.MyGestureListener());

        //刷新图片
        refreshImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arr = new int[3];
                Random random = new Random(System.currentTimeMillis());
                for (int i = 0; i < arr.length; i++) {
                    int num;
                    do {
                        num = random.nextInt(7) + 1;
                    } while (num == arr[0] || num == arr[1] || num == arr[2]);
                    arr[i] = num;
                }
                setViewPager();
            }
        });
    }

    private void setViewPager() {
        //滑动图片
        mViews = new ArrayList<>();
        mDotViewGroup.removeAllViews();
        mDotViews.clear();
        for (int i = 0; i < 3; i++) {
            //设置图片
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(getClothImg(Integer.toString((int) myApplication.user.height), i));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mViews.add(imageView);
            //设置位置点
            ImageView dot = new ImageView(this);
            dot.setImageResource(R.mipmap.unselected);
            dot.setMaxHeight(100);
            dot.setMaxWidth(100);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(30, 30);
            layoutParams.leftMargin = 20;
            dot.setLayoutParams(layoutParams);
            dot.setEnabled(false);

            mDotViewGroup.addView(dot);
            mDotViews.add(dot);
        }

        PagerAdapter mPagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                View child = mViews.get(position);
                container.addView(child);
                return child;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(mViews.get(position));
            }
        };

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(0);
        setDotViews(0);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                setDotViews(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    //设置当前页位置点
    private void setDotViews(int i) {
        for (int j = 0; j < mDotViews.size(); j++) {
            mDotViews.get(j).setImageResource(i == j ? R.mipmap.selected : R.mipmap.unselected);
        }
    }

    private void initView() {
        myApplication = (MyApplication) getApplication();
        mViewPager = findViewById(R.id.viewPager);
        mDotViewGroup = findViewById(R.id.dotGrop);
        shoesImg = findViewById(R.id.shoes);
        necktieImg = findViewById(R.id.necktie);
        wristWatchImg = findViewById(R.id.wristwatch);
        briefCaseImg = findViewById(R.id.briefcase);
        descriptionText = findViewById(R.id.description);
        refreshImg = findViewById(R.id.refresh);
    }

    private Bitmap getClothImg(String height, int index) {
        StringBuilder sb = new StringBuilder("cloth/");
        sb.append("man");
        sb.append("/height/").append(getHeight(Integer.parseInt(height))).append("/").append(getManColor(style)).append("/").append(arr[index]).append(".png");
        try {
            InputStream in = getAssets().open(sb.toString());
            return BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getOtherImg(String other) {
        StringBuilder sb = new StringBuilder("cloth/");
        sb.append("man");
        sb.append("/height/").append(getManColor(style)).append("/").append(other).append(".png");
        try {
            InputStream in = getAssets().open(sb.toString());
            return BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int getHeight(int height) {
        if (height < 300) {
            return 160;
        } else if (height < 350) {
            return 160;
        } else if (height < 400) {
            return 165;
        } else if (height < 450) {
            return 170;
        } else if (height < 500) {
            return 175;
        } else {
            return 180;
        }
    }

    private String getManColor(String style) {
        String color;
        switch (style) {
            case "0":
                color = "black";
                break;
            case "2":
                color = "gray";
                break;
            case "3":
                color = "gray2";
                break;
            case "4":
            case "5":
                color = "tibetan_blue";
                break;
            default:
                color = "blue";
                break;
        }
        return color;
    }

    private String getDescription() {
        String description;
        switch (style) {
            case "0":
                description = (String) this.getResources().getText(R.string.des_man_black);
                break;
            case "2":
                description = (String) this.getResources().getText(R.string.des_man_gray);
                break;
            case "3":
                description = (String) this.getResources().getText(R.string.des_man_gray2);
                break;
            case "4":
            case "5":
                description = (String) this.getResources().getText(R.string.des_man_tibetan_blue);
                break;
            default:
                description = (String) this.getResources().getText(R.string.des_man_blue);
                break;
        }
        return description;
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        //onFling方法的第一个参数是 手指按下的位置， 第二个参数是 手指松开的位置，第三个参数是手指的速度
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float startX = e1.getX();//通过e1.getX（）获得手指按下位置的横坐标
            float endX = e2.getX();//通过e2.getX（）获得手指松开位置的横坐标
            float startY = e1.getY();//通过e1.getY（）获得手指按下位置的纵坐标
            float endY = e2.getY();//通过e2.getY（）获得手指松开的纵坐标
            if ((startX - endX) > 50 && Math.abs(startY - endY) < 200) {
                Intent intent = new Intent(ManWorkClothDetailsActivity.this, SelectModelActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
            }//左滑
            if ((endX - startX) > 50 && Math.abs(startY - endY) < 200) {
                Intent intent = new Intent(ManWorkClothDetailsActivity.this, ComplexionActivity.class);
                intent.putExtra("index", index);
                intent.putExtra("arr", arr);
                intent.putExtra("scene", scene);
                intent.putExtra("style", style);//返回上一界面保存数据
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_left, R.anim.out_from_right);
            }
            //返回值是重点：如果返回值是true则动作可以执行，如果是flase动作将无法执行
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gue.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}

