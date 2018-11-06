/*
 *  Jesse Costales.
 *
 *  Battlesip, Part 3 (Playable)
 *
 *  Full computer playable version that uses a smart Search and Destroy Algorithm
 *  The is alot of inefficient code, but I did try to make the program object oriented
 *  The are four main classes -
 *    Battleship  - the game itself
 *    Board   - the Frame where everything happens
 *    Grid    - one grid for each side - blue & red
 *    Ship    - info, image and ship methods
 */

import java.awt.*;
import java.awt.event.*;
import java.lang.Math;
import java.util.Random;


public class Battleship         // the main class
{

    public static void main(String argv[]) {
        Board board = new Board();

        board.setTitle("Battleship");
        board.setResizable(false);
        board.pack();
        board.setVisible(true);
    }
}

class Board extends Frame    // this is what you see
        implements WindowListener, KeyListener, ActionListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // some constants
    final boolean blue = true;
    final boolean red = false;

    final boolean horz = true;
    final boolean vert = false;

    Random rand = new Random();

    Grid FleetGrid = new Grid(blue, this);     // you
    Grid EnemyGrid = new Grid(red, this);      // the computer
    Panel ControlPanel = new Panel();

    // controls
    Choice shipTypeMenu = new Choice();
    Choice shipViewMenu = new Choice();

    Button resetButton = new Button();
    Button quitButton = new Button();
    Button fireButton = new Button();

    public Board() {
        // Menus to select type and orientation of ship
        shipTypeMenu.addItem("Submarine");
        shipTypeMenu.addItem("Destroyer");
        shipTypeMenu.addItem("Cruiser");
        shipTypeMenu.addItem("Battleship");
        shipTypeMenu.addItem("Carrier");
        ControlPanel.add(shipTypeMenu);

        shipViewMenu.addItem("Horizontal");
        shipViewMenu.addItem("Vertical");
        ControlPanel.add(shipViewMenu);

        resetButton.setLabel("New");
        fireButton.setLabel("Fire");
        quitButton.setLabel("Quit");
        ControlPanel.add(resetButton);
        ControlPanel.add(fireButton);
        ControlPanel.add(quitButton);

        resetButton.addActionListener(this);
        quitButton.addActionListener(this);
        fireButton.addActionListener(this);

        addKeyListener(this);
        addWindowListener(this);

        setLayout(new BorderLayout(5, 0));
        add("West", FleetGrid);
        add("East", EnemyGrid);
        add("South", ControlPanel);
    }

    public void setCoiceEnabled(boolean b) {
        shipTypeMenu.setEnabled(b);
        shipViewMenu.setEnabled(b);
    }

    public void computerShot() {
        FleetGrid.SearchAndDestroy();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }

    // used for testing
    // press the space for firing random shots
    // restart the game by pressing return
    public void keyPressed(KeyEvent e) {
        int x, y;
        int key = e.getKeyCode();

        if ((key == KeyEvent.VK_SPACE) && !EnemyGrid.done) {
            do {
                x = Math.abs(rand.nextInt()) % 10;
                y = Math.abs(rand.nextInt()) % 10;
            } while (EnemyGrid.shots[x][y] != Grid.blank);
            EnemyGrid.play(x, y);
        }

        if ((key == 10) || (key == 3)) {
            setCoiceEnabled(true);
            fireButton.setEnabled(true);

            FleetGrid.reset();
            EnemyGrid.reset();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
        Object target = e.getSource();

        if (target == resetButton) {
            setCoiceEnabled(true);
            fireButton.setEnabled(true);

            FleetGrid.reset();
            EnemyGrid.reset();
        } else if (target == fireButton) {
            int x, y;

            do {
                x = Math.abs(rand.nextInt()) % 10;
                y = Math.abs(rand.nextInt()) % 10;
            } while (EnemyGrid.shots[x][y] != Grid.blank);
            EnemyGrid.play(x, y);
        } else if (target == quitButton) {
            System.exit(0);
        }
    }

}                               // end Board

class Grid extends Canvas implements MouseListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // the playing grids for moving ships and firing shots
    Image hitImage, missImage;
    Ship fleet[] = new Ship[5];
    final int ac = 4;
    final int bs = 3;
    final int cr = 2;
    final int dd = 1;
    final int sm = 0;

    int shots[][] = new int[10][10];
    final static int hit = -1;
    final static int miss = 1;
    final static int blank = 0;

    final boolean blue = true;
    final boolean red = false;

    final boolean horz = true;
    final boolean vert = false;

    Random rand = new Random();
    Board board;
    boolean side, move = true, done = false;
    boolean searchHorz = true, searchEven;

    // constructor
    public Grid(boolean s, Board b) {

        addMouseListener(this);
        setSize(321, 321);

        hitImage = Toolkit.getDefaultToolkit().getImage("resources/hit.gif");
        missImage = Toolkit.getDefaultToolkit().getImage("resources/miss.gif");

        board = b;
        side = s;

        reset();
    }

    public void reset() {
        move = true;
        done = false;
        searchEven = (rand.nextInt() < 0);

        // clear the grid
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++)
                shots[i][j] = blank;
        }

        if (side)
            setBackground(Color.blue);
        else
            setBackground(Color.red);

        for (int i = 0; i < 5; i++)
            fleet[i] = new Ship(i, 0, i, horz);

        // very bad algorithm for random placement
        do {
            do {
                for (int i = 0; i < 5; i++) {
                    int x = Math.abs(rand.nextInt()) % 10;
                    int y = Math.abs(rand.nextInt()) % 10;
                    boolean z = rand.nextInt() > 0;

                    fleet[i].placeShip(x, y, z);
                }
            } while (!isBoardOK());
        } while (!side && (
                // this stuff tries to space the ships around to make them harder to find
                // it isn't that good
                (Math.abs(fleet[ac].x - fleet[bs].x) < 3) ||
                        (Math.abs(fleet[ac].y - fleet[bs].y) < 3) ||
                        (Math.abs(fleet[bs].x - fleet[cr].x) < 2) ||
                        (Math.abs(fleet[bs].y - fleet[cr].y) < 2) ||
                        // try to avoid edges also
                        (fleet[ac].y == 0) || (fleet[ac].y == 9) ||
                        (fleet[ac].x == 0) || (fleet[ac].x == 9) ||
                        (fleet[bs].y == 0) || (fleet[bs].y == 9) ||
                        (fleet[bs].x == 0) || (fleet[bs].x == 9)));

        repaint();
    }

    public boolean isBoardOK() {
        // this is a collision and inbound detection for the ships
        int count = 0;

        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (fleet[0].isMiss(x, y) && fleet[1].isMiss(x, y) &&
                        fleet[2].isMiss(x, y) && fleet[3].isMiss(x, y) && fleet[4].isMiss(x, y))
                    count++;
            }
        }
        return (count == 84);
    }

    public int countShots() {
        int count = 100;

        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (shots[x][y] == blank)
                    count--;
            }
        }
        return count;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {

        int x = e.getX() / 32;
        int y = e.getY() / 32;

        if (x > 9)
            x = 9;
        ;
        if (y > 9)
            y = 9;
        ;

        if (!done)
            play(x, y);
    }

    public void play(int x, int y) {
        int ship = board.shipTypeMenu.getSelectedIndex();
        boolean horz = (board.shipViewMenu.getSelectedIndex() == 0);
        int oldx, oldy;
        boolean oldhorz;

        oldx = fleet[ship].x;
        oldy = fleet[ship].y;
        oldhorz = fleet[ship].horz;

        if (side) {
            if (move)
                fleet[ship].placeShip(x, y, horz);
            if (!isBoardOK())
                fleet[ship].placeShip(oldx, oldy, oldhorz);
        } else if (shots[x][y] == blank) {
            fireShot(x, y);           // do shot on red grid
            board.computerShot();     // get shot on blue grid
        }

        repaint();

        if (board.FleetGrid.fleet[0].isSunk() && board.FleetGrid.fleet[1].isSunk() &&
                board.FleetGrid.fleet[2].isSunk() && board.FleetGrid.fleet[3].isSunk() &&
                board.FleetGrid.fleet[4].isSunk()) {
            System.out.println("Fleet Destroyed in " + (board.FleetGrid.countShots()) + " shots!");
            done = true;
        }

        if (board.EnemyGrid.fleet[0].isSunk() && board.EnemyGrid.fleet[1].isSunk() &&
                board.EnemyGrid.fleet[2].isSunk() && board.EnemyGrid.fleet[3].isSunk() &&
                board.EnemyGrid.fleet[4].isSunk()) {
            System.out.println("Enemy Destroyed in " + (board.EnemyGrid.countShots()) + " shots");
            done = true;
        }
    }

    public boolean fireShot(int x, int y) {
        // check shot for hits and misses
        move = false;
        board.setCoiceEnabled(false);

        shots[x][y] = miss;
        for (int i = sm; i <= ac; i++) {
            if (fleet[i].shootShip(x, y))
                shots[x][y] = hit;
        }
        repaint();
        return (shots[x][y] == hit);
    }

    public boolean isSunken(int x, int y) {
        for (int i = 0; i < 5; i++) {
            if (!fleet[i].isMiss(x, y))
                return fleet[i].isSunk();
        }
        return false;
    }

    public void SearchAndDestroy() {
        // SEARCH & DESTROY ALOGRITHM
        // if a ship is hit but not sunk, try to destroy it.
        // if all hit ships are sunk, search every other grid for ships.
        // this alogrithm also uses some random variable to change the search and destroy
        // pattern so that the pattern is unpredictable.
        int x, y;

        move = false;
        board.setCoiceEnabled(false);

        // DESTROY
        // the destroy is inefficient - alot of nested for loops
        // but effective - ALOT of nested for loops
        // there is also some randomness - see variables searchHorz and searchEven

        // look horizontally
        if (searchHorz) {
            for (x = 0; x < 9; x++) {
                for (y = 0; y < 10; y++) {
                    if (shots[x][y] == hit && shots[x + 1][y] == blank && !isSunken(x, y)) {
                        fireShot(x + 1, y);
                        return;
                    }
                }
            }

            for (x = 1; x < 10; x++) {
                for (y = 0; y < 10; y++) {
                    if (shots[x][y] == hit && shots[x - 1][y] == blank && !isSunken(x, y)) {
                        fireShot(x - 1, y);
                        return;
                    }
                }
            }
        }                           // end searchHorz

        // look vertically
        searchHorz = false;
        for (x = 0; x < 10; x++) {
            for (y = 0; y < 9; y++) {
                if (shots[x][y] == hit && shots[x][y + 1] == blank && !isSunken(x, y)) {
                    if ((x != 0 && shots[x - 1][y] != hit) || (x != 9 && shots[x + 1][y] != hit)) {
                        fireShot(x, y + 1);
                        return;
                    }
                }
            }
        }

        for (x = 0; x < 10; x++) {
            for (y = 1; y < 10; y++) {
                if (shots[x][y] == hit && shots[x][y - 1] == blank && !isSunken(x, y)) {
                    if ((x != 0 && shots[x - 1][y] != hit) || (x != 9 && shots[x + 1][y] != hit))
                        fireShot(x, y - 1);
                    return;
                }
            }
        }

        // second pass
        searchHorz = true;
        for (x = 1; x < 10; x++) {
            for (y = 0; y < 10; y++) {
                if (shots[x][y] == hit && shots[x - 1][y] == blank && !isSunken(x, y)) {
                    fireShot(x - 1, y);
                    return;
                }
            }
        }

        for (x = 0; x < 9; x++) {
            for (y = 0; y < 10; y++) {
                if (shots[x][y] == hit && shots[x + 1][y] == blank && !isSunken(x, y)) {
                    fireShot(x + 1, y);
                    return;
                }
            }
        }

        for (x = 0; x < 10; x++) {
            for (y = 1; y < 10; y++) {
                if (shots[x][y] == hit && shots[x][y - 1] == blank && !isSunken(x, y)) {
                    fireShot(x, y - 1);
                    return;
                }
            }
        }

        for (x = 0; x < 10; x++) {
            for (y = 0; y < 9; y++) {
                if (shots[x][y] == hit && shots[x][y + 1] == blank && !isSunken(x, y)) {
                    fireShot(x, y + 1);
                    return;
                }
            }
        }

        // SEARCH
        // search every other grid
        long count = 0;
        boolean look = true;
        boolean even;

        do {
            count++;
            x = Math.abs(rand.nextInt()) % 10;
            y = Math.abs(rand.nextInt()) % 10;
            if (count > 1000)
                look = false;
            even = ((x % 2) == (y % 2));
        } while (((even != searchEven) || (shots[x][y] != blank)) && look);

        searchHorz = (rand.nextInt() > 0);  // to confuse the enemy

        if (look) {
            fireShot(x, y);
            return;
        }
        // just incase the algorithm gets stuck start looking at all grids
        do {
            x = Math.abs(rand.nextInt()) % 10;
            y = Math.abs(rand.nextInt()) % 10;
        } while (shots[x][y] != blank);

        searchHorz = !searchHorz;
        fireShot(x, y);
    }

    public void plotShot(Graphics g, int x, int y) {
        int sq = shots[x][y];

        if (sq == hit) {
            g.drawImage(hitImage, x * 32, y * 32, this);
        } else if (sq == miss) {
            g.drawImage(missImage, x * 32, y * 32, this);

//            g.setColor(Color.white);
//            g.fillRect(x * 32 + 1, y * 32 + 1, 31, 31);
//            g.setColor(Color.black);
        }
    }

    public void paint(Graphics g) {
        // draw the grid
        g.drawRect(0, 0, 320, 320); // outline
        for (int i = 0; i < 320; i += 32) { // and lines
            g.drawLine(0, i, 319, i);
            g.drawLine(i, 0, i, 319);
        }

        // draw the shots
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++)
                plotShot(g, i, j);
        }

        // draw ship
        if (side) {                 // blue ships
            for (int i = 0; i < 5; i++)
                g.drawImage(fleet[i].image, fleet[i].x * 32, fleet[i].y * 32, this);
        } else {                    // red ships
            for (int i = 0; i < 5; i++) {
                if (fleet[i].isSunk() || done)  // drawn only when sunk
                    g.drawImage(fleet[i].image, fleet[i].x * 32, fleet[i].y * 32, this);
            }
        }
    }

}                               // end Grid

class Ship {

    // ship data
    private int type, size, hits;

    public Image image;
    public int x, y;
    public boolean horz;          // the lay of the ship

    // prefix for the name of the image files
    final String[] gifFilename = {"sm", "dd", "cr", "bs", "ac"};

    // constructor
    public Ship(int type, int x, int y, boolean horz) {
        String gifView;
        String gifType;

        this.type = type;
        this.x = x;
        this.y = y;
        this.horz = horz;
        size = type + 1;
        if (type == 0) size = 2;
        hits = 0;

        // build the name of the image then store it in the ships image
        if (horz)
            gifView = new String("H.gif");
        else
            gifView = new String("V.gif");
        gifType = new String(gifFilename[type]);
        image = Toolkit.getDefaultToolkit().getImage("resources/" + gifType + gifView);
    }

    // move a already created ship
    public void placeShip(int x, int y, boolean horz) {
        String gifView;
        String gifType;

        this.x = x;
        this.y = y;
        this.horz = horz;

        if (horz)
            gifView = new String("H.gif");
        else
            gifView = new String("V.gif");
        gifType = new String(gifFilename[type]);
        image = Toolkit.getDefaultToolkit().getImage("resources/" + gifType + gifView);
    }

    // this is how you shoot a ship
    public boolean shootShip(int x, int y) {
        boolean hit;

        if (horz)
            hit = (y == this.y && x >= this.x && x < this.x + size);
        else
            hit = (x == this.x && y >= this.y && y < this.y + size);

        if (hit && hits < size)
            hits++;

        return hit;
    }

    // methods to find out about the ship
    public boolean isMiss(int x, int y) {
        if (horz)
            return (y != this.y || x < this.x || x >= this.x + size);
        else
            return (x != this.x || y < this.y || y >= this.y + size);
    }

    public boolean isSunk() {
        return (hits >= size);
    }

}
