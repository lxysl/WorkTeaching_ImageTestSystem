package com.benson.face;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FaceResultActivity extends AppCompatActivity {

    private GestureDetector gue;

    private double pointToLine(int x1, int y1, int x2, int y2, int x0,
                               int y0) {
        double space = 0;
        double a, b, c;
        a = lineSpace(x1, y1, x2, y2);// 线段的长度
        b = lineSpace(x1, y1, x0, y0);// (x1,y1)到点的距离
        c = lineSpace(x2, y2, x0, y0);// (x2,y2)到点的距离
        if (c <= 0.000001 || b <= 0.000001) {
            space = 0;
            return space;
        }
        if (a <= 0.000001) {
            space = b;
            return space;
        }
        if (c * c >= a * a + b * b) {
            space = b;
            return space;
        }
        if (b * b >= a * a + c * c) {
            space = c;
            return space;
        }
        double p = (a + b + c) / 2;// 半周长
        double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
        space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
        return space;
    }

    // 计算两点之间的距离
    private double lineSpace(int x1, int y1, int x2, int y2) {
        double lineLength = 0;
        lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2)
                * (y1 - y2));
        return lineLength;
    }

    public static double Angle(int x0, int y0, int x1, int y1, int x2, int y2) {
        double M_PI = 3.1415926535897;

        double ma_x = x1 - x0;
        double ma_y = y1 - y0;
        double mb_x = x2 - x0;
        double mb_y = y2 - y0;
        double v1 = (ma_x * mb_x) + (ma_y * mb_y);
        double ma_val = Math.sqrt(ma_x * ma_x + ma_y * ma_y);
        double mb_val = Math.sqrt(mb_x * mb_x + mb_y * mb_y);
        double cosM = v1 / (ma_val * mb_val);
        double angleAMB = Math.acos(cosM) * 180 / M_PI;

        return angleAMB;
    }


    public String faceType(String jsonStr) {
        try {
            JSONObject object = new JSONObject(jsonStr);
            JSONArray faces = object.getJSONArray("faces");
            int faceCount = faces.length();
            //拿到单独face对象
            JSONObject face = faces.getJSONObject(0);
            System.out.println(face.getJSONObject("attributes").getJSONObject("ethnicity").getString("value"));
            switch (face.getJSONObject("attributes").getJSONObject("ethnicity").getString("value")) {
                case "ASIAN":
                    ((MyApplication) getApplication()).user.complexion = User.Complexion.YELLOW;
                    break;
                case "WHITE":
                    ((MyApplication) getApplication()).user.complexion = User.Complexion.WHITE;
                    break;
                case "BLACK":
                    ((MyApplication) getApplication()).user.complexion = User.Complexion.BLACK;
                    break;
                default:
                    ((MyApplication) getApplication()).user.complexion = User.Complexion.WHEAT;
                    break;
            }
            String mal = face.getJSONObject("attributes").getJSONObject("gender").getString("value");
            //  String mal = face.getJSONObject("attributes").getString("gender");
            JSONObject posObj = face.getJSONObject("landmark");
            int left_two_x = posObj.getJSONObject("contour_left2").getInt("x");
            int left_two_y = posObj.getJSONObject("contour_left2").getInt("y");
            int right_two_x = posObj.getJSONObject("contour_right2").getInt("x");
            int right_two_y = posObj.getJSONObject("contour_right2").getInt("y");
            int chin_x = posObj.getJSONObject("contour_chin").getInt("x");
            int chin_y = posObj.getJSONObject("contour_chin").getInt("y");
            int face_left_x = posObj.getJSONObject("contour_left6").getInt("x");
            int face_left_y = posObj.getJSONObject("contour_left6").getInt("y");
            int face_right_x = posObj.getJSONObject("contour_right6").getInt("x");
            int face_right_y = posObj.getJSONObject("contour_right6").getInt("y");
            double face_length_top = lineSpace(left_two_x, left_two_y, right_two_x, right_two_y);
            double face_length_root = lineSpace(face_left_x, face_left_y, face_right_x, face_left_y);
            //宽脸结果
            double result_weithfae = face_length_top / face_length_root;
            double face_heigh = pointToLine(left_two_x, left_two_y, right_two_x, right_two_y, chin_x, chin_y);
            //长脸结果
            double result_longface = face_heigh / face_length_top;
            //下巴角度
            double result_Angle = Angle(chin_x, chin_y, face_left_x, face_left_y, face_right_x, face_right_y);

            Log.e("TAG", left_two_x + "," + left_two_y + "," + right_two_x + "," + right_two_y + "," + chin_x + "," + chin_y);

            if ("m".equals(((MyApplication) getApplication()).user.gender)) {

                if (result_weithfae < 1.25) {
                    //方脸
                    ((MyApplication) getApplication()).user.facetype = User.FaceType.FANG;
                    return "m_fang";
                } else if (result_longface > 1) {
                    //长脸
                    ((MyApplication) getApplication()).user.facetype = User.FaceType.CHANG;
                    return "m_chang";
                } else if (result_Angle < 87) {
                    //菱形脸
                    ((MyApplication) getApplication()).user.facetype = User.FaceType.LING;
                    return "m_ling";
                } else if (result_Angle >= 87 && result_Angle < 92) {
                    //鹅蛋脸
                    ((MyApplication) getApplication()).user.facetype = User.FaceType.EDAN;
                    return "m_edan";
                } else {
                    //圆脸
                    ((MyApplication) getApplication()).user.facetype = User.FaceType.YUAN;
                    return "m_yuan";
                }
            } else {
                if (result_weithfae < 1.25) {
                    //方脸
                    ((MyApplication) getApplication()).user.facetype = User.FaceType.FANG;
                    return "w_fang";
                } else if (result_longface > 1) {
                    //长脸
                    ((MyApplication) getApplication()).user.facetype = User.FaceType.CHANG;
                    return "w_chang";
                } else if (result_Angle < 87) {
                    //菱形脸
                    ((MyApplication) getApplication()).user.facetype = User.FaceType.LING;
                    return "w_ling";
                } else if (result_Angle >= 87 && result_Angle < 92) {
                    //鹅蛋脸
                    ((MyApplication) getApplication()).user.facetype = User.FaceType.EDAN;
                    return "w_edan";
                } else {
                    //圆脸
                    ((MyApplication) getApplication()).user.facetype = User.FaceType.YUAN;
                    return "w_yuan";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_result);
        LinearLayout hairLayout = findViewById(R.id.hairLayout);
        TextView faceName = findViewById(R.id.faceName);
        ImageView faceImg_1 = findViewById(R.id.faceImg_1);
        ImageView faceImg_2 = findViewById(R.id.faceImg_2);
        ImageView faceImg_3 = findViewById(R.id.faceImg_3);
        TextView faceDes = findViewById(R.id.faceDes);
        String result = ((MyApplication) getApplication()).faceResult;
        if (result != null) {
            String face = faceType(result);
            if (!face.equals("")) {
                if ("m".equals(((MyApplication) getApplication()).user.gender)) {
                    switch (((MyApplication) getApplication()).user.facetype) {
                        case FANG:
                            faceName.setText("方脸");
                            faceImg_1.setImageResource(R.drawable.m_fang_1);
                            faceImg_2.setImageResource(R.drawable.m_fang_2);
                            faceImg_3.setImageResource(R.drawable.m_fang_3);
                            faceDes.setText(this.getResources().getText(R.string.m_fang));
                            break;
                        case CHANG:
                            faceName.setText("长脸");
                            faceImg_1.setImageResource(R.drawable.m_chang_1);
                            faceImg_2.setImageResource(R.drawable.m_chang_2);
                            faceImg_3.setImageResource(R.drawable.m_chang_3);
                            faceDes.setText(this.getResources().getText(R.string.m_chang));
                            break;
                        case LING:
                            faceName.setText("菱形脸");
                            faceImg_1.setImageResource(R.drawable.m_ling_1);
                            faceImg_2.setImageResource(R.drawable.m_ling_2);
                            faceImg_3.setImageResource(R.drawable.m_ling_3);
                            faceDes.setText(this.getResources().getText(R.string.m_ling));
                            break;
                        case EDAN:
                            faceName.setText("鹅蛋脸");
                            faceImg_1.setImageResource(R.drawable.m_edan_1);
                            faceImg_2.setImageResource(R.drawable.m_edan_2);
                            faceImg_3.setImageResource(R.drawable.m_edan_3);
                            faceDes.setText(this.getResources().getText(R.string.m_edan));
                            break;
                        case YUAN:
                            faceName.setText("圆脸");
                            faceImg_1.setImageResource(R.drawable.m_yuan_1);
                            faceImg_2.setImageResource(R.drawable.m_yuan_2);
                            faceImg_3.setImageResource(R.drawable.m_yuan_3);
                            faceDes.setText(this.getResources().getText(R.string.m_yuan));
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (((MyApplication) getApplication()).user.facetype) {
                        case FANG:
                            faceName.setText("方脸");
                            faceImg_1.setImageResource(R.drawable.w_fang);
                            hairLayout.removeView(faceImg_2);
                            hairLayout.removeView(faceImg_3);
                            faceDes.setText(this.getResources().getText(R.string.w_fang));
                            break;
                        case CHANG:
                            faceName.setText("长脸");
                            faceImg_1.setImageResource(R.drawable.w_chang);
                            hairLayout.removeView(faceImg_2);
                            hairLayout.removeView(faceImg_3);
                            faceDes.setText(this.getResources().getText(R.string.w_chang));
                            break;
                        case LING:
                            faceName.setText("菱形脸");
                            faceImg_1.setImageResource(R.drawable.w_ling);
                            hairLayout.removeView(faceImg_2);
                            hairLayout.removeView(faceImg_3);
                            faceDes.setText(this.getResources().getText(R.string.w_ling));
                            break;
                        case EDAN:
                            faceName.setText("鹅蛋脸");
                            faceImg_1.setImageResource(R.drawable.w_edan);
                            hairLayout.removeView(faceImg_2);
                            hairLayout.removeView(faceImg_3);
                            faceDes.setText(this.getResources().getText(R.string.w_edan));
                            break;
                        case YUAN:
                            faceName.setText("圆脸");
                            faceImg_1.setImageResource(R.drawable.w_yuan);
                            hairLayout.removeView(faceImg_2);
                            hairLayout.removeView(faceImg_3);
                            faceDes.setText(this.getResources().getText(R.string.w_yuan));
                            break;
                        default:
                            break;
                    }
                }
            }
            Toast.makeText(FaceResultActivity.this, "左滑可查看详情以及其他配件的推荐", Toast.LENGTH_SHORT).show();
            gue = new GestureDetector(FaceResultActivity.this, new MyGestureListener());
        }
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
                startActivity(new Intent(FaceResultActivity.this, HairAndGlassesActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
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
