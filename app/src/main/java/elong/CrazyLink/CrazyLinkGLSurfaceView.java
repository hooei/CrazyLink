/**********************************************************
 * 项目名称：山寨腾讯“爱消除”游戏7日教程
 * 作          者：郑敏新
 * 腾讯微博：SuperCube3D
 * 日          期：2013年12月
 * 声          明：版权所有   侵权必究
 * 本源代码供网友研究学习OpenGL ES开发Android应用用，
 * 请勿全部或部分用于商业用途
 ********************************************************/


package elong.CrazyLink;

import elong.CrazyLink.CrazyLinkConstant.E_SCENARIO;
import elong.CrazyLink.Core.ControlCenter;
import elong.CrazyLink.Interaction.ScreenTouch;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Message;
import android.view.MotionEvent;

/**
 * 该类提供了一个OpenGL ES场景渲染器，通过onDrawFrame 方法，将要绘制的图案渲染后输出。
 */
public class CrazyLinkGLSurfaceView extends GLSurfaceView {

    private SceneRenderer mRenderer;    // Scene renderer
    Context mContext;

    static boolean m_bThreadRun = false;

    static ControlCenter controlCenter;

    ScreenTouch screenTouch;

    public CrazyLinkGLSurfaceView(CrazyLinkActivity activity) {
        super(activity);
        mContext = this.getContext();
        mRenderer = new SceneRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY); // set to actively (non-passive) render

        if (!m_bThreadRun) {
            m_bThreadRun = true;
            controlCenter = new ControlCenter(mContext);
            //ctlExchange = new CtlExchange(col1, row1, col2, row2);
            new Thread() {
                public void run() {
                    while (true) {
                        try {
                            controlCenter.run();
                            Thread.sleep(CrazyLinkConstant.DELAY_MS);// 50 ms
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (screenTouch != null) {
            if (ControlCenter.mScene == E_SCENARIO.GAME) {
                screenTouch.touchGameView(e);
            } else if (ControlCenter.mScene == E_SCENARIO.MENU) {
                screenTouch.touchMenuView(e);
            } else if (ControlCenter.mScene == E_SCENARIO.RESULT) {
                screenTouch.touchResultView(e);
            }
        }
        return true;
    }

    private class SceneRenderer implements GLSurfaceView.Renderer {

        public void onDrawFrame(GL10 gl) {
            gl.glShadeModel(GL10.GL_SMOOTH);        // shade mode: smooth
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);    // clear color buffer and depth buffer
            gl.glMatrixMode(GL10.GL_MODELVIEW);     // matrix mode: model view
            gl.glLoadIdentity();                    // set current matrix: unit matrix
            gl.glTranslatef(0f, 0f, -10f);

            if (ControlCenter.mScene == E_SCENARIO.GAME) {
                controlCenter.drawGameScene(gl);
            } else if (ControlCenter.mScene == E_SCENARIO.MENU) {
                controlCenter.drawMenuScene(gl);
            } else if (ControlCenter.mScene == E_SCENARIO.RESULT) {
                controlCenter.drawResultScene(gl);
            }

        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {

            CrazyLinkConstant.REAL_WIDTH = width;
            CrazyLinkConstant.REAL_HEIGHT = height;
            CrazyLinkConstant.translateRatio = (float) width / height;
            CrazyLinkConstant.screentRatio = (float) width / height;
            CrazyLinkConstant.ADP_SIZE = CrazyLinkConstant.UNIT_SIZE * CrazyLinkConstant.VIEW_HEIGHT / height * width / CrazyLinkConstant.VIEW_WIDTH;
            screenTouch = new ScreenTouch(mContext, width, height);
            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL10.GL_PROJECTION);        // set current matrix: projection matrix
            gl.glLoadIdentity();                        // set current matrix: unit matrix

            gl.glOrthof(-CrazyLinkConstant.screentRatio * CrazyLinkConstant.GRID_NUM / 2,
                    CrazyLinkConstant.screentRatio * CrazyLinkConstant.GRID_NUM / 2,
                    -1 * CrazyLinkConstant.GRID_NUM / 2,
                    1 * CrazyLinkConstant.GRID_NUM / 2, 10, 100);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glDisable(GL10.GL_DITHER);           // disable dither
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);    // set fast hint
            gl.glClearColor(0, 0, 0, 0);            // screen background color black RGBA
            gl.glShadeModel(GL10.GL_SMOOTH);        // set shade mode: smooth
            gl.glEnable(GL10.GL_DEPTH_TEST);        // enable depth test

            // something else
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
            gl.glEnable(GL10.GL_ALPHA_TEST);
            gl.glAlphaFunc(GL10.GL_GREATER, 0.1f);

            controlCenter.initTexture(gl);
            controlCenter.initDraw(gl);
            if (ControlCenter.mScene == E_SCENARIO.GAME) {
                Message msg = new Message();
                msg.what = ControlCenter.LOADING_START;
                ControlCenter.mHandler.sendMessage(msg);
            }

        }

    }


}


