package YummyList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

/**
 * ========================================================
 *  MemberFrame — หน้าสั่งอาหารหลักของ Member
 * ========================================================
 * extends JFrame → เป็นหน้าต่างหลักของ session การสั่งอาหาร
 *
 * Layout หลัก (BorderLayout):
 *   NORTH  → top bar (แสดงแต้ม + ปุ่ม Logout)
 *   CENTER → JSplitPane แบ่ง 2 ฝั่ง:
 *     LEFT  → ตารางเมนูอาหาร + spinner qty + ปุ่ม "Add to Cart"
 *     RIGHT → ตารางตะกร้าสินค้า + สรุปราคา/เวลา + ปุ่ม Checkout
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← สร้างโดย  LoginFrame.doMemberLogin() → new MemberFrame(member, mm, men)
 *   ← สร้างโดย  ReceiptFrame (Order Again) → new MemberFrame(member, mm, men)
 *   → เรียก      MenuManager  (getAllMenus, getMenuById)
 *   → เรียก      MemberManager (saveToFile หลัง checkout)
 *   → สร้าง      Order        ตอน checkout
 *   → เปิด       ReceiptFrame หลัง checkout สำเร็จ
 *   → เปิด       LoginFrame   เมื่อ Logout
 */
public class MemberFrame extends JFrame {

    /** member — ผู้ใช้ที่ login อยู่ใน session นี้ */
    private final Member member;

    /** memberManager — ใช้ saveToFile() หลังแต้มเปลี่ยน */
    private final MemberManager memberManager;

    /** menuManager — ใช้ getAllMenus() และ getMenuById() */
    private final MenuManager menuManager;

    // ==================== Table Models ====================

    /**
     * menuModel — DefaultTableModel สำหรับตารางเมนูทางซ้าย
     * Columns: ID, Name, Price (฿), Time (min)
     */
    private DefaultTableModel menuModel;

    /**
     * cartModel — DefaultTableModel สำหรับตารางตะกร้าทางขวา
     * Columns: Name, Qty, Subtotal
     */
    private DefaultTableModel cartModel;

    // ==================== ตะกร้าสินค้า (Cart) ====================

    /**
     * cartMenus — List เก็บ Menu object ที่อยู่ในตะกร้า
     * index ตรงกับ cartQtys (cartMenus[i] กับ cartQtys[i] คือคู่เดียวกัน)
     */
    private final ArrayList<Menu>    cartMenus = new ArrayList<>();

    /**
     * cartQtys — List เก็บจำนวนของแต่ละเมนูในตะกร้า
     * index ตรงกับ cartMenus
     */
    private final ArrayList<Integer> cartQtys  = new ArrayList<>();

    // ==================== UI Components ====================

    /** totalLabel    — JLabel แสดงราคารวม "฿ X.XX" ใน summary card */
    private JLabel totalLabel;

    /** cookTimeLabel — JLabel แสดงเวลาทำอาหารรวม "X min" */
    private JLabel cookTimeLabel;

    /** topPointsLabel — JLabel แสดงแต้มบน top bar "⭐ X pts" */
    private JLabel topPointsLabel;

    /** qtySpinner — JSpinner เลือกจำนวนก่อน Add to Cart (1-99) */
    private JSpinner qtySpinner;

    /** menuTable — JTable แสดงเมนูทั้งหมด (เลือกเพื่อ add ลงตะกร้า) */
    private JTable menuTable;

    /**
     * Constructor — สร้างหน้าต่าง MemberFrame พร้อม UI ทั้งหมด
     *
     * JSplitPane:
     *   HORIZONTAL_SPLIT → แบ่งซ้าย-ขวา
     *   setDividerLocation(530) → เส้นแบ่งอยู่ที่ 530px จากซ้าย
     *   setDividerSize(6) → เส้นแบ่งกว้าง 6px
     *   setBorder(null) → ไม่มีขอบ
     *
     * @param member  Member ที่ login อยู่
     * @param mm      MemberManager
     * @param men     MenuManager
     */
    public MemberFrame(Member member, MemberManager mm, MenuManager men) {
        this.member        = member;
        this.memberManager = mm;
        this.menuManager   = men;

        setTitle("YummyList — " + member.getUsername());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UI.BACKGROUND);

        add(buildTopBar(), BorderLayout.NORTH);

        // JSplitPane แบ่ง menu panel (ซ้าย) กับ cart panel (ขวา)
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildMenuPanel(),   // ซ้าย
                buildCartPanel()    // ขวา
        );
        split.setDividerLocation(530);
        split.setDividerSize(6);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        setVisible(true);
    }

    /**
     * buildTopBar() — สร้าง top bar สีส้มพร้อมแต้มและปุ่ม Logout
     *
     * topPointsLabel:
     *   แสดง "⭐ X pts" บน top bar
     *   อัปเดตใน refreshSummary() ทุกครั้งที่ตะกร้าเปลี่ยน
     *   และหลัง checkout ผ่าน member.getPoints()
     *
     * ปุ่ม Logout:
     *   สีขาวโปร่งแสง → dispose() + new LoginFrame(...)
     *
     * @return JPanel top bar
     */
    private JPanel buildTopBar() {
        topPointsLabel = new JLabel("⭐ " + member.getPoints() + " pts");
        topPointsLabel.setFont(UI.F_BOLD);
        topPointsLabel.setForeground(Color.WHITE);

        UI.Btn logout = new UI.Btn("Logout", new Color(255,255,255,60), new Color(255,255,255,100));
        logout.setForeground(Color.WHITE);
        logout.setPreferredSize(new Dimension(90, 32));
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame(memberManager, menuManager);
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(topPointsLabel);
        right.add(logout);
        return UI.topBar("🍜 YummyList", right);
    }

    /**
     * buildMenuPanel() — สร้าง panel ตารางเมนูทางซ้าย
     *
     * ประกอบด้วย:
     *   - UI.title("Menu")
     *   - JTable (menuModel):
     *       Columns: ID, Name, Price (฿), Time (min)
     *       SINGLE_SELECTION: เลือกได้ครั้งละ 1 แถว
     *       ไม่มี ListSelectionListener (ไม่ auto-load form ไม่มี form ทางนี้)
     *   - Bar ล่าง: "Qty:" + JSpinner(1-99) + ปุ่ม "+ Add to Cart"
     *
     * refreshMenuTable() → โหลดเมนูทั้งหมดครั้งแรก
     *
     * @return JPanel ฝั่งเมนู
     */
    private JPanel buildMenuPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UI.BACKGROUND);
        p.setBorder(new EmptyBorder(16, 16, 16, 8));
        p.add(UI.title("Menu"), BorderLayout.NORTH);

        // DefaultTableModel สำหรับเมนู
        menuModel = new DefaultTableModel(
                new String[]{"ID","Name","Price (฿)","Time (min)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        menuTable = new JTable(menuModel);
        UI.styleTable(menuTable);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // ปรับความกว้าง column
        menuTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        menuTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        refreshMenuTable();
        p.add(UI.scroll(menuTable), BorderLayout.CENTER);

        // Bar ล่าง: Qty Spinner + ปุ่ม Add
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        // SpinnerNumberModel(value, min, max, step): เริ่ม=1, min=1, max=99, step=1
        qtySpinner.setPreferredSize(new Dimension(70, 36));
        qtySpinner.setFont(UI.F_BODY);

        UI.Btn btnAdd = UI.btnPrimary("+ Add to Cart");
        btnAdd.setPreferredSize(new Dimension(140, UI.BTN_H));
        btnAdd.addActionListener(e -> addToCart());

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        bar.add(UI.label("Qty:"));
        bar.add(qtySpinner);
        bar.add(btnAdd);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    /**
     * buildCartPanel() — สร้าง panel ตะกร้าสินค้าทางขวา
     *
     * ประกอบด้วย:
     *   - UI.title("Your Cart")
     *   - JTable (cartModel):
     *       Columns: Name, Qty, Subtotal
     *       ไม่มี selection listener
     *   - buildSummary() → panel สรุปราคา/เวลา/แต้ม + ปุ่ม
     *
     * @return JPanel ฝั่งตะกร้า
     */
    private JPanel buildCartPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UI.BACKGROUND);
        p.setBorder(new EmptyBorder(16, 8, 16, 16));
        p.add(UI.title("Your Cart"), BorderLayout.NORTH);

        // DefaultTableModel สำหรับตะกร้า
        cartModel = new DefaultTableModel(
                new String[]{"Name","Qty","Subtotal"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable cartTable = new JTable(cartModel);
        UI.styleTable(cartTable);
        p.add(UI.scroll(cartTable), BorderLayout.CENTER);
        p.add(buildSummary(), BorderLayout.SOUTH);
        return p;
    }

    /**
     * buildSummary() — สร้าง panel สรุปราคา + ปุ่ม Checkout
     *
     * UI.Card ประกอบด้วย GridLayout(3, 2, 8, 6):
     *   Row 1: "Total:"        | totalLabel (ส้ม, bold 16pt)
     *   Row 2: "Cook Time:"    | cookTimeLabel
     *   Row 3: "Your Points:"  | แต้มปัจจุบัน
     *
     * ปุ่ม 2 ปุ่ม (GridLayout(1, 2)):
     *   "Clear Cart" (เทา)  → clearCart()
     *   "Checkout →" (ส้ม)  → doCheckout()
     *
     * @return JPanel สรุปพร้อมปุ่ม
     */
    private JPanel buildSummary() {
        UI.Card card = new UI.Card();
        card.setLayout(new GridLayout(3, 2, 8, 6));

        card.add(UI.label("Total:"));
        totalLabel = new JLabel("฿ 0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        totalLabel.setForeground(UI.PRIMARY);
        card.add(totalLabel);

        card.add(UI.label("Cook Time:"));
        cookTimeLabel = new JLabel("0 min");
        cookTimeLabel.setFont(UI.F_BOLD);
        card.add(cookTimeLabel);

        card.add(UI.label("Your Points:"));
        JLabel pts = new JLabel("⭐ " + member.getPoints());
        pts.setFont(UI.F_BOLD);
        card.add(pts);

        // ปุ่ม
        UI.Btn btnClear    = UI.btnGray("Clear Cart");
        UI.Btn btnCheckout = UI.btnPrimary("Checkout →");
        btnClear.setPreferredSize(new Dimension(0, UI.BTN_H));
        btnCheckout.setPreferredSize(new Dimension(0, UI.BTN_H));
        btnClear.addActionListener(e -> clearCart());
        btnCheckout.addActionListener(e -> doCheckout());

        JPanel btns = new JPanel(new GridLayout(1, 2, 8, 0));
        btns.setOpaque(false);
        btns.add(btnClear);
        btns.add(btnCheckout);

        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.add(card, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    // ==================== Logic Methods ====================

    /**
     * refreshMenuTable() — โหลดเมนูทั้งหมดจาก MenuManager ลง menuTable
     *
     * setRowCount(0) → ล้างก่อน
     * getAllMenus().forEach() → เพิ่มแต่ละเมนูเป็น 1 แถว
     *
     * เรียกจาก: constructor (ผ่าน buildMenuPanel)
     * หมายเหตุ: ไม่ refresh ระหว่าง session (เมนูไม่เปลี่ยน ถ้า admin ไม่แก้)
     */
    private void refreshMenuTable() {
        menuModel.setRowCount(0);
        menuManager.getAllMenus().forEach(m -> menuModel.addRow(new Object[]{
                m.getId(),
                m.getName(),
                String.format("%.2f", m.getPrice()),
                m.getCookTime()
        }));
    }

    /**
     * addToCart() — เพิ่มเมนูที่เลือกลงตะกร้า
     *
     * Flow:
     * 1. getSelectedRow() → ตรวจว่าเลือกแถวหรือเปล่า (-1 = ไม่ได้เลือก)
     * 2. ดึง ID จาก column 0 + qty จาก qtySpinner
     * 3. getMenuById(id) → ได้ Menu object
     * 4. ตรวจว่าเมนูนี้มีในตะกร้าแล้วหรือไม่ (วนลูป cartMenus):
     *    มีแล้ว → บวก qty เพิ่ม + อัปเดต cell ใน cartModel
     *    ยังไม่มี → เพิ่ม menu+qty ใหม่ใน cartMenus/cartQtys + addRow ใน cartModel
     * 5. refreshSummary() → อัปเดตราคา/เวลา/แต้ม
     *
     * หมายเหตุ:
     *   cartMenus, cartQtys, cartModel ต้อง sync กันเสมอ
     *   index เดียวกันหมายถึง item เดียวกัน
     */
    private void addToCart() {
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a menu item first.");
            return;
        }
        int id  = (int) menuModel.getValueAt(row, 0);   // ดึง ID จาก column 0
        int qty = (int) qtySpinner.getValue();

        Menu selected = menuManager.getMenuById(id);
        if (selected == null) return;

        // ตรวจว่ามีในตะกร้าแล้วหรือไม่
        for (int i = 0; i < cartMenus.size(); i++) {
            if (cartMenus.get(i).getId() == id) {
                // พบเมนูซ้ำ → บวก qty
                int newQty = cartQtys.get(i) + qty;
                cartQtys.set(i, newQty);
                // อัปเดตข้อมูลใน cartModel โดยตรง
                cartModel.setValueAt(newQty, i, 1);   // column 1 = Qty
                cartModel.setValueAt(String.format("%.2f", selected.getPrice() * newQty), i, 2);  // column 2 = Subtotal
                refreshSummary();
                return;
            }
        }
        // ไม่มีในตะกร้า → เพิ่มใหม่
        cartMenus.add(selected);
        cartQtys.add(qty);
        cartModel.addRow(new Object[]{
                selected.getName(),
                qty,
                String.format("%.2f", selected.getPrice() * qty)
        });
        refreshSummary();
    }

    /**
     * clearCart() — ล้างตะกร้าทั้งหมด
     *
     * ล้างทั้ง 3 อย่างพร้อมกัน:
     *   cartMenus.clear() → ล้าง Menu list
     *   cartQtys.clear()  → ล้าง Qty list
     *   cartModel.setRowCount(0) → ล้างแถวใน JTable
     * refreshSummary() → reset ราคา/เวลาเป็น 0
     *
     * เรียกจาก: ปุ่ม "Clear Cart" และ doCheckout() (หลัง checkout สำเร็จ)
     */
    private void clearCart() {
        cartMenus.clear();
        cartQtys.clear();
        cartModel.setRowCount(0);
        refreshSummary();
    }

    /**
     * refreshSummary() — คำนวณและอัปเดต totalLabel, cookTimeLabel, topPointsLabel
     *
     * วนลูปผ่าน cartMenus/cartQtys:
     *   total += price × qty
     *   time  += cookTime × qty
     *
     * อัปเดต:
     *   totalLabel    → "฿ X.XX"
     *   cookTimeLabel → "X min"
     *   topPointsLabel → "⭐ X pts" (อัปเดตบน top bar ด้วย)
     *
     * เรียกจาก: addToCart(), clearCart(), doCheckout() (ผ่าน clearCart)
     */
    private void refreshSummary() {
        double total = 0;
        int time = 0;
        for (int i = 0; i < cartMenus.size(); i++) {
            total += cartMenus.get(i).getPrice() * cartQtys.get(i);
            time  += cartMenus.get(i).getCookTime() * cartQtys.get(i);
        }
        totalLabel.setText(String.format("฿ %.2f", total));
        cookTimeLabel.setText(time + " min");
        topPointsLabel.setText("⭐ " + member.getPoints() + " pts");
    }

    /**
     * doCheckout() — ดำเนินการ checkout ทั้งกระบวนการ
     *
     * Flow หลัก:
     * 1. ตรวจว่าตะกร้าไม่ว่าง
     * 2. สร้าง Order object + addItem ทุกรายการจากตะกร้า
     * 3. calculateTotal() → ได้ยอดรวม
     * 4. ถ้ามีแต้ม (member.getPoints() > 0):
     *    วนลูปถามจนกว่าจะได้ค่าที่ถูกต้อง:
     *      - showPointsDialog() → dialog ถามว่าจะใช้แต้มเท่าไหร่
     *      - ตรวจ usePoint == -1 (กดปิด dialog) → return ยกเลิก checkout
     *      - ตรวจ usePoint > member.getPoints() → warning + วนใหม่
     *      - ตรวจ usePoint > total → warning + วนใหม่
     * 5. หักแต้มและคำนวณยอดสุทธิ:
     *    total -= usePoint
     *    member.addPoints(-usePoint)
     * 6. คำนวณแต้มที่ได้รับ (5% ของยอดสุทธิ):
     *    earned = (int)(total * 0.05)
     *    member.addPoints(earned)
     * 7. memberManager.saveToFile() → บันทึกแต้มใหม่ลงไฟล์
     * 8. new ReceiptFrame(...) → แสดงใบเสร็จ
     * 9. clearCart() + dispose() → ล้างตะกร้าและปิดหน้านี้
     */
    private void doCheckout() {
        if (cartMenus.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty!");
            return;
        }

        // สร้าง Order จากตะกร้าปัจจุบัน
        Order order = new Order();
        for (int i = 0; i < cartMenus.size(); i++)
            order.addItem(cartMenus.get(i), cartQtys.get(i));

        double total    = order.calculateTotal();
        int    usePoint = 0;

        // ถามการใช้แต้มถ้ามีแต้ม
        if (member.getPoints() > 0) {
            while (true) {
                try {
                    usePoint = showPointsDialog(total);

                    if (usePoint == -1) return;   // กดปิด dialog = ยกเลิก checkout

                    if (usePoint > member.getPoints()) {
                        JOptionPane.showMessageDialog(this,
                                "You don't have enough points \nYou only have " + member.getPoints() + " points!",
                                "Warning", JOptionPane.WARNING_MESSAGE);
                        continue;   // วนลูปถามใหม่
                    }
                    if (usePoint > (int) total) {
                        JOptionPane.showMessageDialog(this,
                                "You can't use points over the price",
                                "Warning", JOptionPane.WARNING_MESSAGE);
                        continue;
                    }
                    break;   // ค่าถูกต้อง ออกจาก loop
                } catch (NumberFormatException e) {
                    continue;   // ป้องกัน parse error (จริงๆ spinner ไม่น่าเกิด แต่ safety net)
                }
            }
        }

        // คำนวณยอดสุทธิและแต้ม
        total -= usePoint;               // หักแต้มที่ใช้จากยอดรวม
        member.addPoints(-usePoint);      // หักแต้มจาก member

        int earned = (int) (total * 0.05);   // แต้มที่ได้ = 5% ของยอดสุทธิ (ตัดเศษ)
        member.addPoints(earned);             // บวกแต้มที่ได้

        memberManager.saveToFile();   // บันทึกแต้มใหม่ลงไฟล์ทันที

        // เปิดใบเสร็จ
        new ReceiptFrame(order, total, order.calculateCookTime(), earned, member, memberManager, menuManager);
        clearCart();
        dispose();
    }

    /**
     * showPointsDialog(double total) — แสดง dialog ให้เลือกใช้แต้ม
     *
     * Dialog แบบ Modal (true) → block UI จนกว่าจะปิด
     * ประกอบด้วย:
     *   - top bar "⭐ Use Your Points"
     *   - UI.Card แสดงข้อมูล: แต้มคงเหลือ, ยอดรวม, อัตราแลกเปลี่ยน (1pt=1฿)
     *   - JSpinner (0 ถึง MAX_INT) สำหรับระบุแต้มที่จะใช้
     *   - KeyAdapter ดักการพิมพ์: อนุญาตแค่ตัวเลขและ Backspace
     *   - ปุ่ม "Skip" (usePoint=0) และ "Confirm"
     *
     * Return value:
     *   -1  = กดปิด dialog โดยไม่กดปุ่ม (ยกเลิก checkout)
     *    0  = กด Skip (ไม่ใช้แต้ม)
     *   >0  = จำนวนแต้มที่ต้องการใช้
     *
     * int[] result = {-1}:
     *   ใช้ array แทน int เพราะ lambda ต้องการ effectively final variable
     *   result[0] เปลี่ยนได้จากใน lambda
     *
     * spinner.commitEdit():
     *   บังคับให้ spinner อ่านค่าจาก text field
     *   throw ParseException ถ้า format ผิด
     *
     * @param total  ยอดรวมก่อนหักแต้ม (แสดงใน dialog)
     * @return จำนวนแต้มที่เลือกใช้ (int), -1 ถ้าปิด dialog
     */
    private int showPointsDialog(double total) {
        JDialog dlg = new JDialog(this, "Use Your Points", true);   // true = modal
        dlg.setSize(340, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UI.BACKGROUND);

        // Top bar ของ dialog
        dlg.add(UI.topBar("⭐ Use Your Points", null), BorderLayout.NORTH);

        // JSpinner สำหรับกรอกแต้มที่จะใช้
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 2147483647, 1));
        spinner.setFont(UI.F_BODY);
        spinner.setPreferredSize(new Dimension(0, 36));

        /*
         * JFormattedTextField — text field ที่ซ่อนอยู่ใน JSpinner.DefaultEditor
         * ดึงมาเพื่อเพิ่ม KeyAdapter ดักการพิมพ์
         *
         * KeyAdapter.keyTyped():
         *   e.getKeyChar() → ตัวอักษรที่พิมพ์
         *   isDigit(c) → ตัวเลข 0-9
         *   VK_BACK_SPACE → ปุ่ม Backspace
         *   e.consume() → ยกเลิกการพิมพ์ตัวอักษรนั้น
         */
        JFormattedTextField txt = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        txt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!(Character.isDigit(c) || c == java.awt.event.KeyEvent.VK_BACK_SPACE)) {
                    e.consume();   // บล็อกตัวอักษรที่ไม่ใช่ตัวเลข
                }
            }
        });

        // Card แสดงข้อมูลใน dialog
        UI.Card card = new UI.Card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1; c.gridx = 0;
        c.insets = new Insets(4, 4, 4, 4);

        // แถวข้อมูล
        c.gridy = 0; card.add(infoRow("Your Points:", "⭐ " + member.getPoints() + " pts"), c);
        c.gridy = 1; card.add(infoRow("Order Total:", String.format("฿ %.2f", total)), c);
        c.gridy = 2; card.add(infoRow("Rate:", "1 pt = 1 ฿"), c);
        c.gridy = 3; card.add(new JSeparator(), c);   // เส้นคั่น
        c.gridy = 4; card.add(UI.label("Points to use:"), c);
        c.gridy = 5; card.add(spinner, c);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(UI.BACKGROUND);
        body.setBorder(new javax.swing.border.EmptyBorder(12, 12, 4, 12));
        body.add(card);
        dlg.add(body, BorderLayout.CENTER);

        // int[] result ใช้แทน int ธรรมดาเพราะ lambda ต้องการ effectively final
        int[] result = {-1};   // default = -1 (ปิดโดยไม่กดปุ่ม)

        UI.Btn btnSkip = UI.btnGray("Skip");
        UI.Btn btnOk   = UI.btnPrimary("Confirm");

        // Skip → ไม่ใช้แต้ม (result=0) แล้วปิด dialog
        btnSkip.addActionListener(e -> {
            result[0] = 0;
            dlg.dispose();
        });

        // Confirm → อ่านค่า spinner + ปิด dialog
        btnOk.addActionListener(e -> {
            try {
                spinner.commitEdit();   // force sync text field → spinner value
                result[0] = (Integer) spinner.getValue();
                dlg.dispose();
            } catch (Exception ex) {
                // ค่าไม่ถูกต้อง (copy-paste ข้อความลงมา) → แจ้ง error ไม่ปิด dialog
                JOptionPane.showMessageDialog(dlg,
                        "กรุณากรอกตัวเลขให้ถูกต้อง!", "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                spinner.setValue(0);   // reset เป็น 0
            }
        });

        // Footer: Skip | Confirm
        JPanel footer = new JPanel(new GridLayout(1, 2, 8, 0));
        footer.setBackground(UI.BACKGROUND);
        footer.setBorder(new javax.swing.border.EmptyBorder(0, 12, 12, 12));
        footer.add(btnSkip);
        footer.add(btnOk);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);   // block จนกว่า dialog จะปิด (เพราะ modal=true)
        return result[0];        // คืนค่าหลัง dialog ปิด
    }

    /**
     * infoRow(String label, String value) — สร้าง JPanel แสดงข้อมูล label: value
     *
     * ใช้ใน showPointsDialog() เพื่อแสดง:
     *   "Your Points: | ⭐ X pts"
     *   "Order Total: | ฿ X.XX"
     *   "Rate:        | 1 pt = 1 ฿"
     *
     * BorderLayout:
     *   WEST → label (F_BODY, TEXT_GRAY)
     *   EAST → value (F_BOLD)
     *
     * @param label  ข้อความหัวข้อ
     * @param value  ค่าที่แสดง
     * @return JPanel row โปร่งแสง
     */
    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(UI.F_BODY);
        l.setForeground(UI.TEXT_GRAY);
        JLabel v = new JLabel(value);
        v.setFont(UI.F_BOLD);
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }
}
