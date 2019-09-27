package mogura;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import sql.Dao;

public class Mogura extends JFrame {
    public Mogura() {
        super("モグラたたき");
        setContentPane(new MOGURATatakiCanvas());
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Mogura();
    }
}

class MOGURATatakiCanvas extends JPanel {

    private static final long serialVersionUID = 1L;

    static final int START_WAITING = 0;
    static final int PLAYING       = 1;
    static final int ENDING        = 2;

    static final int IMG_TITLE        = 0;
    static final int IMG_MOGURA_NORMAL  = 1;
    static final int IMG_MOGURA_DAMAGED = 2;
    static final int IMG_ENDING       = 3;

    static final int MOGURA_SELECTED = 0;
    static final int BOMB_SELECTED = 1;

    static final int SPEED_LOW    = 3;
    static final int SPEED_MIDDLE = 2;
    static final int SPEED_HIGH   = 1;

    static final String IMG_NAMES[] = {       //画像
        "C:\\Users\\alterbo_PC-23\\Desktop\\mogura1img\\gd_test.gif",        //640 x 480
        "C:\\Users\\alterbo_PC-23\\Desktop\\mogura1img\\mogura2-removebg-preview.png",  //70 x 70
        "C:\\Users\\alterbo_PC-23\\Desktop\\mogura1img\\棺桶-removebg-preview.png", //70 x 70
        "C:\\Users\\alterbo_PC-23\\Desktop\\mogura1img\\棺桶-removebg-preview.png"        //640 x 480
    };

    static final int TIMER_INTERVAL = 400;     //タイマー処理間隔 0.4秒
    static final int TIMEUP         = 30000;   //タイムアップの時間 60秒

    static final int CANVAS_WIDTH  = 640;
    static final int CANVAS_HEIGHT = 480;

    static final int OBJECT_WIDTH  = 70;       // モグラの幅
    static final int OBJECT_HEIGHT = 70;       // モグラの高さ

    static final Point LOCATION[] = {          // 出現位置
            new Point( 50, 150),
            new Point(150, 150),
            new Point(250, 150),
            new Point(350, 150),
            new Point(450, 150),
            new Point(550, 150),
            new Point( 50, 350),
            new Point(150, 350),
            new Point(250, 350),
            new Point(350, 350),
            new Point(450, 350),
            new Point(550, 350)
    };

    static final ArrayList<Image> images = new ArrayList<Image>();

    static {
        loadGameImages();
    }

    static void loadGameImages() {
        for (String name : IMG_NAMES) {
            try {
                FileInputStream in = new FileInputStream(name);
                Image img = ImageIO.read(in);
                images.add(img);
                in.close();
            } catch (IOException ex) {
                System.err.println("画像ファイル " + name + " が存在しません。");
                System.exit(1);
            }
        }
    }

    final GameState gameStates[] = {
        new StartWaiting(),
        new Playing(),
        new Ending()
    };

    private int     currentState     = START_WAITING;
    private int     bestScore        = 0;
    private int     score            = 0;
    private boolean overBest         = false;          // ハイスコアをとったらtrue
    private long    startTime        = 0L;
    private int     selectedLocation = 0;              // ランダムで選択した出現位置
    private int     selectedObject   = 0;              // ランダムで選択した出現オブジェクト
    private int     callCounter      = 0;
    private boolean hitObject        = false;
    private boolean bool             =false;

    private Random random = new Random(System.currentTimeMillis());

    private Timer timer = new Timer(TIMER_INTERVAL, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            callCounter++;
            if (hitObject) hitObject = false;
            long pastTime = System.currentTimeMillis() - startTime;
            if (pastTime >= TIMEUP) {
                timer.stop();
                currentState = ENDING;
                if (score > bestScore) {
                    bestScore = score;
                    overBest = true;
                }
            } else if (pastTime <= TIMEUP * 0.2) {
                if (callCounter % SPEED_LOW != 0) return;
            } else if (pastTime <= TIMEUP * 0.85) {
                if (callCounter % SPEED_MIDDLE != 0) return;
            }

            int loc = 0;
            while ((loc = selectLocation()) == selectedLocation);

            selectedLocation = loc;

            MOGURATatakiCanvas.this.repaint();
        }

        private int selectLocation() {
            return Math.abs(random.nextInt()) % LOCATION.length;
        }

    });

    public MOGURATatakiCanvas() {
        setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                gameStates[currentState].mouseClicked(e);
            }
        });
    }

    public void paint(Graphics g) {
        gameStates[currentState].paint(g);
    }

    interface GameState {
        public void paint(Graphics g);
        public void mouseClicked(MouseEvent e);
    }

    class StartWaiting implements GameState {
        public void paint(Graphics g) {
            g.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            Image img = images.get(IMG_TITLE);
            g.drawImage(img, 0, 0, MOGURATatakiCanvas.this);
        }

        public void mouseClicked(MouseEvent e) {
            // タイトル画面をクリックしたらゲームスタート
            score            = 0;
            overBest         = false;
            selectedLocation = 0;
            selectedObject   = 0;
            callCounter      = 0;
            hitObject        = false;
            startTime        = System.currentTimeMillis();
            currentState     = PLAYING;
             timer.start();
            MOGURATatakiCanvas.this.repaint();
        }
    }

    class Playing implements GameState {
    	int hit;
        public void paint(Graphics g) {

            g.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            int index = IMG_MOGURA_NORMAL;
            hitObject = bool;
            if (selectedObject == MOGURA_SELECTED && hitObject) {
                index = IMG_MOGURA_DAMAGED;
                bool = false;
            }
            Image img = images.get(index);
            int x = LOCATION[selectedLocation].x;
            int y = LOCATION[selectedLocation].y;
            g.drawImage(img, x, y, MOGURATatakiCanvas.this);
            g.drawString("得点: " + score, 10, 10);
            hit = index;
        }

        public void mouseClicked(MouseEvent e) {
            // 当たり判定
            int x = LOCATION[selectedLocation].x;
            int y = LOCATION[selectedLocation].y;
            Rectangle rect = new Rectangle(x, y, OBJECT_WIDTH, OBJECT_HEIGHT);
            if (rect.contains(e.getPoint()) && hit == IMG_MOGURA_NORMAL) {
            	score += 100;
            	bool =true;
            }
            MOGURATatakiCanvas.this.repaint();

        }
    }

    class Ending implements GameState {
    	Dao dao  = new Dao();
        public void paint(Graphics g) {
            g.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            Image img = images.get(IMG_ENDING);
            dao.insert(null, score);
            dao.select();
            g.drawImage(img, 0, 0, MOGURATatakiCanvas.this);
            g.drawString("現在の最高得点: " + dao.getHiscore(), 200, 200);
            g.drawString("今回の得点: " + score, 200, 230);
//            if (overBest) g.drawString("ハイスコアです。おめでとう", 200, 260);

        }

        public void mouseClicked(MouseEvent e) {
            currentState = START_WAITING;
            MOGURATatakiCanvas.this.repaint();
        }
    }
}