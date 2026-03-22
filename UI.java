package YummyList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * ========================================================
 *  UI — รวม Theme สี, Font, และ Component สำเร็จรูปทั้งหมด
 * ========================================================
 * Utility/Factory class รวมทุกอย่างเกี่ยวกับ UI ไว้ที่เดียว
 * ประกอบด้วย:
 *   1. Constants — สี, font, ขนาด
 *   2. Inner class Btn   — ปุ่มโค้งมนพร้อม hover effect
 *   3. Inner class Card  — Panel ขาวโค้งมน
 *   4. Factory methods   — สร้าง Component พร้อม style
 *
 * ไม่มี constructor — ใช้ผ่าน static ทั้งหมด (ยกเว้น Btn, Card ที่ต้อง new)
 *
 * ความสัมพันธ์กับ class อื่น:
 *   → ใช้ใน LoginFrame, MemberFrame, AdminFrame, ReceiptFrame
 *     ทุก Frame เรียกใช้ UI.xxx() เพื่อสร้าง Component และใช้สี
 */
public class UI {

    // ==================== สี ====================
    // Color(r, g, b) — RGB 0-255

    /** สีหลักของแอป (ส้ม) — ใช้บน top bar, ปุ่ม primary, header ตาราง */
    static final Color PRIMARY      = new Color(255, 107, 53);

    /** สีส้มเข้มกว่า — ใช้เป็น hover state ของ primary button */
    static final Color PRIMARY_DARK = new Color(220, 80, 30);

    /** สีพื้นหลังหน้าจอ (เทาอ่อน) — ใช้เป็น background ของ JFrame ทุกหน้า */
    static final Color BACKGROUND   = new Color(245, 245, 245);

    /** สีขาว — ใช้เป็น background ของ Card และ JTable */
    static final Color SURFACE      = Color.WHITE;

    /** สีข้อความหลัก (ดำเกือบดำ) — ใช้กับ label, JTable ทั่วไป */
    static final Color TEXT         = new Color(33, 33, 33);

    /** สีข้อความรอง (เทา) — ใช้กับ label คำอธิบาย/หัวข้อย่อย */
    static final Color TEXT_GRAY    = new Color(117, 117, 117);

    /** สีเขียว — ใช้บน header ReceiptFrame และปุ่ม "Save Edit" */
    static final Color SUCCESS      = new Color(76, 175, 80);

    /** สีแดง — ใช้บน top bar AdminFrame และปุ่ม "Delete" */
    static final Color DANGER       = new Color(244, 67, 54);

    /** สีเส้นขอบ (เทาอ่อน) — ใช้บน input field, scroll pane */
    static final Color BORDER       = new Color(224, 224, 224);

    /** สีแถวสลับในตาราง (ส้มอ่อนมาก) — แถวคู่ใช้สีนี้ */
    static final Color ROW_ALT      = new Color(255, 248, 245);

    // ==================== Font ====================
    // Font(family, style, size)
    // Font.BOLD = 1, Font.PLAIN = 0

    /** Font หัวข้อใหญ่ — ใช้กับ UI.title() เช่น "Menu", "Your Cart" */
    static final Font F_TITLE = new Font("SansSerif", Font.BOLD,  22);

    /** Font ตัวหนา — ใช้กับ label, ค่าสำคัญ (ราคา, แต้ม) */
    static final Font F_BOLD  = new Font("SansSerif", Font.BOLD,  14);

    /** Font ปกติ — ใช้กับ input field, ข้อความทั่วไปในตาราง */
    static final Font F_BODY  = new Font("SansSerif", Font.PLAIN, 13);

    /** Font ปุ่ม — ใช้กับ Btn และ header ของตาราง */
    static final Font F_BTN   = new Font("SansSerif", Font.BOLD,  13);

    // ==================== ขนาด ====================

    /** RADIUS — ความโค้งมนของ rounded corner (px) ใช้กับ Btn และ Card */
    static final int RADIUS = 12;

    /** BTN_H — ความสูงมาตรฐานของปุ่ม (px) ตั้งค่าด้วย setPreferredSize */
    static final int BTN_H  = 40;


    // ==================== BUTTON ====================

    /**
     * Btn — ปุ่มโค้งมนพร้อม hover effect แบบ custom
     *
     * extends JButton แต่ override การวาดด้วย paintComponent()
     * เพื่อให้ได้ rounded corner และ color change เมื่อ hover
     *
     * ใช้สร้างผ่าน factory method:
     *   UI.btnPrimary("text") → ส้ม
     *   UI.btnSuccess("text") → เขียว
     *   UI.btnDanger("text")  → แดง
     *   UI.btnGray("text")    → เทา
     *
     * ใช้ใน: ทุก Frame ที่มีปุ่ม
     */
    static class Btn extends JButton {

        /** bg — สีพื้นหลังปกติของปุ่ม (ไม่ hover) */
        private final Color bg;

        /** hover — สีพื้นหลังเมื่อเมาส์ชี้อยู่บนปุ่ม */
        private final Color hover;

        /** on — state ว่าเมาส์อยู่บนปุ่มหรือไม่ (true=hover) */
        private boolean on;

        /**
         * Constructor — สร้างปุ่มพร้อมกำหนดสี 2 state
         *
         * setOpaque(false), setContentAreaFilled(false):
         *   ปิด Swing default background เพื่อให้ paintComponent() ควบคุมเองได้
         * setBorderPainted(false): ปิด default border
         * setFocusPainted(false): ปิด focus ring เมื่อคลิก
         * setCursor(HAND_CURSOR): เปลี่ยน cursor เป็นมือเมื่อชี้บนปุ่ม
         *
         * MouseAdapter: listen mousEntered/Exited เพื่อ toggle on flag
         *   on=true  → repaint() → paintComponent() ใช้สี hover
         *   on=false → repaint() → paintComponent() ใช้สี bg
         *
         * @param text   ข้อความบนปุ่ม
         * @param bg     สีปกติ
         * @param hover  สีเมื่อ hover
         */
        Btn(String text, Color bg, Color hover) {
            super(text);
            this.bg = bg; this.hover = hover;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(F_BTN);
            setForeground(Color.WHITE);   // ข้อความบนปุ่มสีขาว
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            // MouseAdapter: track hover state
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { on = true;  repaint(); }
                public void mouseExited (MouseEvent e) { on = false; repaint(); }
            });
        }

        /**
         * paintComponent(Graphics g) — วาดปุ่มแบบ custom
         *
         * override จาก JComponent
         * Graphics2D — เวอร์ชัน 2D ของ Graphics รองรับ antialias และ shape
         *
         * setRenderingHint(ANTIALIASING):
         *   ทำให้ขอบโค้งมนไม่มีรอยหยัก (smooth edges)
         *
         * RoundRectangle2D.Float(x, y, w, h, arcW, arcH):
         *   วาด rectangle ที่มีมุมโค้ง ขนาด arcW=arcH=RADIUS
         *
         * g2.setColor(on ? hover : bg):
         *   ถ้า hover → ใช้สีเข้มกว่า, ถ้าไม่ hover → ใช้สีปกติ
         *
         * g2.dispose(): คืน Graphics2D resource (ป้องกัน memory leak)
         * super.paintComponent(g): วาดข้อความ (text) และ focus indicator ต่อ
         *
         * @param g  Graphics context ที่ Swing ส่งมาให้
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(on ? hover : bg);   // สลับสีตาม hover state
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS, RADIUS));
            g2.dispose();
            super.paintComponent(g);   // วาดข้อความบนปุ่มต่อ
        }
    }

    // ==================== Button Factory Methods ====================
    // รับแค่ข้อความ คืน Btn พร้อมสีที่เหมาะสม

    /** btnPrimary — ปุ่มสีส้ม (ส้มเข้มเมื่อ hover) — ใช้กับ action หลัก */
    static Btn btnPrimary(String t) { return new Btn(t, PRIMARY, PRIMARY_DARK); }

    /** btnSuccess — ปุ่มสีเขียว — ใช้กับ action บันทึก/สำเร็จ */
    static Btn btnSuccess(String t) { return new Btn(t, SUCCESS, new Color(46, 125, 50)); }

    /** btnDanger — ปุ่มสีแดง — ใช้กับ action ลบ/อันตราย */
    static Btn btnDanger(String t)  { return new Btn(t, DANGER, new Color(198, 40, 40)); }

    /** btnGray — ปุ่มสีเทา — ใช้กับ action รอง เช่น Clear, Skip */
    static Btn btnGray(String t)    { return new Btn(t, new Color(158,158,158), new Color(117,117,117)); }


    // ==================== CARD PANEL ====================

    /**
     * Card — Panel สีขาวโค้งมน (custom painted)
     *
     * extends JPanel แต่ override paintComponent() เพื่อวาด rounded background
     * setOpaque(false) ทำให้ Swing ไม่วาด background เอง
     * แล้ว paintComponent() วาด rounded white rectangle แทน
     *
     * ใช้ใน:
     *   - LoginFrame   → wrap form fields
     *   - AdminFrame   → wrap form เพิ่ม/แก้ไขเมนู
     *   - MemberFrame  → buildSummary() - ราคารวม/แต้ม
     *   - MemberFrame  → showPointsDialog() - dialog ใช้แต้ม
     *   - ReceiptFrame → รายละเอียดใบเสร็จ
     */
    static class Card extends JPanel {

        /**
         * Constructor — ตั้งค่า default ของ Card
         * setOpaque(false): ให้ paintComponent ควบคุม background เอง
         * EmptyBorder(20, 24, 20, 24): padding ด้านใน
         */
        Card() {
            setOpaque(false);
            setBorder(new EmptyBorder(20, 24, 20, 24));
        }

        /**
         * paintComponent(Graphics g) — วาด Card แบบ custom
         *
         * ใช้หลักการเดียวกับ Btn.paintComponent:
         *   Graphics2D + antialiasing + RoundRectangle2D
         * สี SURFACE (ขาว) ทำให้ Card ดูลอยบนพื้น BACKGROUND (เทา)
         *
         * @param g  Graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(SURFACE);   // สีขาวสำหรับ Card
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS, RADIUS));
            g2.dispose();
            // ไม่เรียก super.paintComponent(g) → ให้ child component วาดทับเอง
        }
    }


    // ==================== INPUT FIELDS ====================

    /**
     * field() — สร้าง JTextField พร้อม style
     *
     * CompoundBorder:
     *   LineBorder(BORDER, 1, true) → เส้นขอบบางๆ สีเทา, rounded
     *   EmptyBorder(6, 10, 6, 10)  → padding ด้านใน (top, left, bottom, right)
     *
     * ใช้ใน: LoginFrame, AdminFrame (ทุก input text ที่ไม่ใช่ password)
     *
     * @return JTextField พร้อม style
     */
    static JTextField field() {
        JTextField f = new JTextField();
        f.setFont(F_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        return f;
    }

    /**
     * password() — สร้าง JPasswordField พร้อม style (เหมือน field() แต่ซ่อนข้อความ)
     *
     * JPasswordField แสดงผลเป็น '●' แทนตัวอักษรจริง
     * ต้องเรียก getPassword() (ไม่ใช่ getText()) เพื่ออ่านรหัสผ่าน
     *
     * ใช้ใน: LoginFrame (ทุก field password)
     *
     * @return JPasswordField พร้อม style
     */
    static JPasswordField password() {
        JPasswordField f = new JPasswordField();
        f.setFont(F_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        return f;
    }

    /**
     * label(String text) — สร้าง JLabel หัวข้อ field (bold)
     *
     * ใช้ F_BOLD + TEXT color
     * ใช้ใน: ทุก Frame เป็น label บอกชื่อ input
     *
     * @param text  ข้อความ label
     * @return JLabel พร้อม style
     */
    static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_BOLD);
        l.setForeground(TEXT);
        return l;
    }

    /**
     * title(String text) — สร้าง JLabel หัวเรื่องใหญ่
     *
     * ใช้ F_TITLE (22pt bold) + TEXT color
     * ใช้ใน: AdminFrame ("All Menus"), MemberFrame ("Menu", "Your Cart")
     *
     * @param text  ข้อความหัวเรื่อง
     * @return JLabel ขนาดใหญ่ style
     */
    static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_TITLE);
        l.setForeground(TEXT);
        return l;
    }


    // ==================== TABLE ====================

    /**
     * styleTable(JTable t) — ตั้งค่า style ให้ JTable พร้อมใช้งาน
     *
     * การตั้งค่าพื้นฐาน:
     *   setFont(F_BODY)       → font ของ cell
     *   setRowHeight(36)      → ความสูงแต่ละแถว
     *   setShowGrid(false)    → ซ่อนเส้น grid ระหว่าง cell
     *   setIntercellSpacing(0,0) → ไม่มีช่องว่างระหว่าง cell
     *   setSelectionBackground/Foreground → สีเมื่อเลือกแถว
     *   setFillsViewportHeight → ให้ table ขยายเต็ม scroll pane
     *
     * DefaultTableCellRenderer (body):
     *   override getTableCellRendererComponent()
     *   setBorder(EmptyBorder) → padding ใน cell
     *   ถ้าไม่ได้เลือก (sel=false):
     *     แถวคู่  (row % 2 == 0) → SURFACE (ขาว)
     *     แถวคี่  (row % 2 != 0) → ROW_ALT (ส้มอ่อน)
     *
     * JTableHeader renderer:
     *   ความสูง header = 42px
     *   background = PRIMARY (ส้ม), foreground = ขาว
     *   ใช้ F_BTN font
     *   setOpaque(true) สำคัญ — ถ้าไม่ set บาง L&F จะ override สี
     *
     * ใช้ใน: AdminFrame.buildTablePanel(), MemberFrame.buildMenuPanel(),
     *         MemberFrame.buildCartPanel()
     *
     * @param t  JTable ที่ต้องการตั้งค่า style
     */
    static void styleTable(JTable t) {
        t.setFont(F_BODY);
        t.setRowHeight(36);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(255, 230, 220));   // ส้มอ่อน
        t.setSelectionForeground(TEXT);
        t.setBackground(SURFACE);
        t.setFillsViewportHeight(true);

        // Renderer สลับสีแถว
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));   // padding ซ้าย-ขวาใน cell
                if (!sel) setBackground(row % 2 == 0 ? SURFACE : ROW_ALT);   // สลับสีแถว
                return this;
            }
        });

        // Renderer header สีส้ม
        JTableHeader h = t.getTableHeader();
        h.setPreferredSize(new Dimension(h.getWidth(), 42));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                l.setBackground(PRIMARY);       // พื้นหลังส้ม
                l.setForeground(Color.WHITE);   // ข้อความขาว
                l.setFont(F_BTN);
                l.setBorder(new EmptyBorder(0, 12, 0, 12));
                l.setOpaque(true);   // ต้อง true ไม่งั้น L&F บางตัว override สี
                return l;
            }
        });
    }


    // ==================== SCROLL PANE ====================

    /**
     * scroll(JTable t) — สร้าง JScrollPane ครอบ JTable พร้อม style
     *
     * LineBorder(BORDER, 1, true) → เส้นขอบบางๆ rounded
     * ไม่มี scrollbar แสดงถาวร — แสดงเมื่อเนื้อหาเกินเท่านั้น
     *
     * ใช้ใน: AdminFrame.buildTablePanel(), MemberFrame (buildMenuPanel, buildCartPanel)
     *
     * @param t  JTable ที่ต้องการ wrap ด้วย scroll
     * @return JScrollPane พร้อม style
     */
    static JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        return sp;
    }


    // ==================== TOP BAR ====================

    /**
     * topBar(String titleText, JPanel right) — สร้าง top bar สีส้ม
     *
     * BorderLayout:
     *   WEST → JLabel title (ชื่อแอป/หน้า)
     *   EAST → JPanel right (ปุ่ม logout, แสดงแต้ม ฯลฯ)
     *
     * ถ้า right == null → ไม่ add panel ทางขวา (ใช้ใน showPointsDialog top bar)
     *
     * ใช้ใน:
     *   - MemberFrame.buildTopBar() → "🍜 YummyList" + แต้ม + Logout
     *   - AdminFrame.buildTopBar()  → "🔧 Admin Panel" + Logout (สี DANGER)
     *   - MemberFrame.showPointsDialog() → "⭐ Use Your Points" (right=null)
     *
     * @param titleText  ข้อความ title ทางซ้าย
     * @param right      Panel ทางขวา (null ได้)
     * @return JPanel top bar พร้อม style
     */
    static JPanel topBar(String titleText, JPanel right) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PRIMARY);
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel t = new JLabel(titleText);
        t.setFont(new Font("SansSerif", Font.BOLD, 20));
        t.setForeground(Color.WHITE);
        bar.add(t, BorderLayout.WEST);
        if (right != null) bar.add(right, BorderLayout.EAST);   // ถ้ามี panel ขวา
        return bar;
    }
}
