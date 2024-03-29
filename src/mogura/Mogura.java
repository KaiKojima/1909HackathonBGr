package mogura;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
        setLocationRelativeTo(null);
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
    static final int IMG_KUSA = 4;

    static final int MOGURA_SELECTED = 0;
    static final int BOMB_SELECTED = 1;

    static final int SPEED_LOW    = 4;
    static final int SPEED_MIDDLE = 3;
    static final int SPEED_HIGH   = 2;

    static final String IMG_NAMES[] = {       //画像
        ".\\infomation\\img\\gd_test.gif",        //640 x 480
        ".\\infomation\\img\\mogura_icon.png",  //70 x 70
        ".\\infomation\\img\\棺桶-removebg-preview.png", //70 x 70
        ".\\infomation\\img\\sult2.png"        //640 x 480
        , ".\\infomation\\img\\kusa.jpg"
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

    private String name = "ななし";
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
    private long pastTime = 0L;

    private Random random = new Random(System.currentTimeMillis());

    private Timer repaintTime = new Timer( 10, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			pastTime = System.currentTimeMillis() - startTime;

			if (pastTime >= TIMEUP) {
                timer.stop();
                repaintTime.stop();
                currentState = ENDING;
                if (score > bestScore) {
                    bestScore = score;
                    overBest = true;
                }

            	JLabel label = new JLabel("おつかれさまでした！！");
            	JOptionPane.showMessageDialog(MOGURATatakiCanvas.this, label, "OK", JOptionPane.INFORMATION_MESSAGE);

            }



			MOGURATatakiCanvas.this.repaint();
		}
    });

    private Timer timer = new Timer(TIMER_INTERVAL, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            callCounter++;


            /*
            if (pastTime >= TIMEUP) {
                timer.stop();
                repaintTime.stop();
                currentState = ENDING;
                if (score > bestScore) {
                    bestScore = score;
                    overBest = true;
                }

            	JLabel label = new JLabel("おつかれさまでした！！");
            	JOptionPane.showMessageDialog(MOGURATatakiCanvas.this, label, "OK", JOptionPane.INFORMATION_MESSAGE);

            } else*/ if (pastTime <= TIMEUP * 0.2) {
                if (callCounter % SPEED_LOW != 0) {
                	return;
                }
            } else if (pastTime <= TIMEUP * 0.85) {
                if (callCounter % SPEED_MIDDLE != 0) {
                	return;
                }
            }else {
            	 if (callCounter % SPEED_HIGH != 0) {
            		 return;
            	 }
            }

            int loc = 0;
            while ((loc = selectLocation()) == selectedLocation);

            selectedLocation = loc;
            if (hitObject) hitObject = false;

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
        	name = "ななし";
            score            = 0;
            overBest         = false;
            selectedLocation = 0;
            selectedObject   = 0;
            callCounter      = 0;
            hitObject        = false;
            currentState     = PLAYING;

            String s = JOptionPane.showInputDialog( "名前を入力してスタート");
            if( s != null && s.length() > 0 ) {
            	if( s.length() > 10 ) {
            		s = s.substring(0, 10);
            	}
            	name = s;
            }

            startTime        = System.currentTimeMillis();
             timer.start();
            repaintTime.start();
            MOGURATatakiCanvas.this.repaint();
        }
    }

    class Playing implements GameState {
    	int hit;
        public void paint(Graphics g) {

            g.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

            Image img = images.get(IMG_KUSA);
            g.drawImage(img, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, MOGURATatakiCanvas.this);

            int index = IMG_MOGURA_NORMAL;
            if (selectedObject == MOGURA_SELECTED && hitObject) {
                index = IMG_MOGURA_DAMAGED;
            }
            img = images.get(index);
            int x = LOCATION[selectedLocation].x;
            int y = LOCATION[selectedLocation].y;
            g.drawImage(img, x, y, MOGURATatakiCanvas.this);


            long nokori = (30000 - pastTime)/1000;
            String msg = "残り時間：" + nokori + "秒   得点: " + score;


            Font font = new Font("ゴシック明朝", Font.BOLD, 16);
            g.setFont(font);
            g.setColor(Color.RED);
            for(int ty=-2; ty<2; ty++) {
            	for(int tx=-2; tx<2; tx++) {
            		g.drawString(msg, 10+tx, 20+ty);
            	}
            }

            g.setColor(Color.WHITE);
            g.drawString(msg, 10, 20);
            hit = index;
        }

        public void mouseClicked(MouseEvent e) {
            // 当たり判定
//        	System.out.println(e);
            int x = LOCATION[selectedLocation].x;
            int y = LOCATION[selectedLocation].y;
            Rectangle rect = new Rectangle(x, y, OBJECT_WIDTH, OBJECT_HEIGHT);
            if (rect.contains(e.getPoint()) && hitObject == false/*&& hit == IMG_MOGURA_NORMAL*/) {
            	score += 100;
            	hitObject =true;
            }
            MOGURATatakiCanvas.this.repaint();

        }
    }

    class Ending implements GameState {
    	Dao dao  = new Dao();
        public void paint(Graphics g) {
            g.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            Image img = images.get(IMG_ENDING);
            dao.insert( name, score);
            dao.select();
            g.drawImage(img, 0, 0, MOGURATatakiCanvas.this);

            //スコア表示
            Font font = new Font("ゴシック明朝", Font.BOLD, 20);
            g.setFont(font);

            String msg1 = "現在の最高得点: " + dao.getHiscore() + "    " + dao.getHiName();
            String msg2 = "今回の得点: " + score + "    " + name;

            g.setColor(Color.WHITE);
            for(int y=-2; y<2; y++) {
            	for(int x=-2; x<2; x++) {
            		 g.drawString(msg1, 200+x, 200+y);
                     g.drawString(msg2, 200+x, 230+y);
            	}
            }
            g.setColor(Color.BLACK);
            g.drawString(msg1, 200, 200);
            g.drawString(msg2, 200, 230);
//            if (overBest) g.drawString("ハイスコアです。おめでとう", 200, 260);

        }

        public void mouseClicked(MouseEvent e) {

            currentState = START_WAITING;
            MOGURATatakiCanvas.this.repaint();
        }
    }
}