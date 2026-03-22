package YummyList;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * ========================================================
 *  ReceiptFrame — หน้าแสดงใบเสร็จหลังจาก Checkout
 * ========================================================
 * extends JFrame → เป็นหน้าต่างอิสระ (DISPOSE_ON_CLOSE ไม่ปิดโปรแกรม)
 *
 * แสดงข้อมูล:
 *   - รายการอาหารที่สั่ง (จาก Order.getOrderSummary())
 *   - ยอดรวมหลังหักแต้ม
 *   - เวลาทำอาหาร
 *   - แต้มที่ได้รับ (earned) และแต้มรวม (member.getPoints())
 *
 * Layout หลัก (BorderLayout):
 *   NORTH  → header สีเขียว "✅ Order Confirmed!"
 *   CENTER → Card (รายละเอียด order + สรุป) ใน JScrollPane
 *   SOUTH  → ปุ่ม "Order Again"
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← สร้างโดย  MemberFrame.doCheckout()
 *               new ReceiptFrame(order, total, cookTime, earned, member, mm, men)
 *   → เรียก      MessageLogger.save() บันทึก receipt ลงไฟล์
 *   → เปิด       MemberFrame เมื่อกด "Order Again"
 *   → รับ        Order, Member, MemberManager, MenuManager
 */
public class ReceiptFrame extends JFrame {

    /**
     * Constructor — สร้างและแสดงหน้าใบเสร็จทั้งหมด
     *
     * @param order     Object Order ที่ประกอบด้วยรายการอาหารทั้งหมด
     * @param total     ยอดรวมหลังหักแต้มแล้ว (double, บาท)
     * @param cookTime  เวลาทำอาหารรวม (int, นาที)
     * @param earned    แต้มที่ได้รับจาก checkout นี้ (int = 5% ของ total)
     * @param member    Member ที่ทำการสั่งซื้อ
     * @param mm        MemberManager ส่งต่อให้ MemberFrame
     * @param men       MenuManager ส่งต่อให้ MemberFrame
     */
    public ReceiptFrame(Order order, double total, int cookTime, int earned,
                        Member member, MemberManager mm, MenuManager men) {
        setTitle("Receipt");
        setSize(380, 480);
        setLocationRelativeTo(null);   // กึ่งกลางหน้าจอ

        // DISPOSE_ON_CLOSE → ปิดแค่หน้าต่างนี้ ไม่ exit โปรแกรม
        // (ต่างจาก EXIT_ON_CLOSE ของ Frame อื่น)
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UI.BACKGROUND);

        // ==================== NORTH: Header สีเขียว ====================
        JPanel header = new JPanel();
        header.setBackground(UI.SUCCESS);   // สีเขียว — บอกว่าสำเร็จ
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("✅  Order Confirmed!", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title);
        add(header, BorderLayout.NORTH);

        // ==================== CENTER: Card รายละเอียด ====================
        UI.Card card = new UI.Card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));   // จัดเรียงแนวตั้ง

        /*
         * JTextArea แสดงรายการอาหาร
         * order.getOrderSummary() คืน String รูปแบบ:
         *   ========= ORDER LIST =========
         *   Fried Rice x 2 = 100.0
         *   ===============================
         *
         * replace() ตัด header/footer ออก เหลือแค่รายการ
         * trim() ตัด whitespace หัว-ท้าย
         */
        String items = order.getOrderSummary()
                .replace("========= ORDER LIST =========\n", "")
                .replace("===============================", "")
                .trim();

        JTextArea area = new JTextArea(items);
        area.setFont(UI.F_BODY);
        area.setEditable(false);        // ป้องกันแก้ไข
        area.setOpaque(false);          // โปร่งแสง เห็น Card พื้นหลัง
        area.setForeground(UI.TEXT);
        card.add(area);
        card.add(Box.createVerticalStrut(10));   // ช่องว่าง 10px
        card.add(separator());                    // เส้นคั่น
        card.add(Box.createVerticalStrut(10));

        // แสดงสรุป: ราคา, เวลา, แต้ม
        card.add(row("Total After Discount:", String.format("฿ %.2f", total)));
        card.add(row("Cook Time:", cookTime + " min"));
        card.add(row("Earned Points (5%):", "+ " + earned + " pts"));
        card.add(row("Total Points:", "⭐ " + member.getPoints() + " pts"));

        /*
         * JScrollPane ครอบ Card
         * setBorder(empty) → ไม่มีขอบรอบ scroll pane
         * getViewport().setBackground(BACKGROUND) → พื้นหลัง viewport ตรงกัน
         */
        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UI.BACKGROUND);
        add(scroll, BorderLayout.CENTER);

        // ==================== บันทึก Receipt ลงไฟล์ ====================
        /*
         * MessageLogger.save() — append ลง message.txt
         * ข้อมูลที่บันทึก:
         *   - Header
         *   - order.getOrderSummary() — รายการอาหารทั้งหมด
         *   - ยอดรวม, เวลา, แต้มที่ได้, แต้มรวม
         * String concatenation ด้วย + (ยาวแต่ทำครั้งเดียวไม่ใช่ใน loop)
         */
        MessageLogger.save(
                "========== RECEIPT ==========\n" +
                order.getOrderSummary() +
                "\nTotal After Discount: " + total +
                "\nCook Time: " + cookTime + " minutes" +
                "\nEarned Points: " + earned +
                "\nTotal Points: " + member.getPoints() +
                "\n============================="
        );

        // ==================== SOUTH: ปุ่ม Order Again ====================
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(UI.BACKGROUND);
        footer.setBorder(new EmptyBorder(8, 0, 12, 0));

        UI.Btn btn = UI.btnPrimary("Order Again");
        btn.setPreferredSize(new Dimension(160, UI.BTN_H));
        btn.addActionListener(e -> {
            dispose();   // ปิด ReceiptFrame
            new MemberFrame(member, mm, men);   // เปิด MemberFrame ใหม่ (reset ตะกร้า)
        });
        footer.add(btn);
        add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * row(String label, String value) — สร้าง JPanel แสดง label: value คู่กัน
     *
     * BorderLayout:
     *   WEST → JLabel ชื่อหัวข้อ (สีเทา, F_BODY)
     *   EAST → JLabel ค่า (สีดำ, F_BOLD)
     * EmptyBorder(3, 0, 3, 0) → padding บน-ล่างเล็กน้อย
     *
     * ใช้สร้าง:
     *   "Total After Discount: | ฿ 130.00"
     *   "Cook Time:            | 34 min"
     *   "Earned Points (5%):   | + 6 pts"
     *   "Total Points:         | ⭐ 156 pts"
     *
     * @param label  ข้อความหัวข้อ (ซ้าย, สีเทา)
     * @param value  ค่า (ขวา, bold)
     * @return JPanel row
     */
    private JPanel row(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);   // โปร่งแสง เห็น Card พื้นหลัง
        p.setBorder(new EmptyBorder(3, 0, 3, 0));
        JLabel l = new JLabel(label); l.setFont(UI.F_BODY); l.setForeground(UI.TEXT_GRAY);
        JLabel v = new JLabel(value); v.setFont(UI.F_BOLD);
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    /**
     * separator() — สร้างเส้นคั่นแนวนอน
     *
     * JSeparator → เส้นแนวนอน (HORIZONTAL ค่า default)
     * setForeground(BORDER) → สีเทาอ่อน
     * setMaximumSize(MAX_VALUE, 1) → ขยายเต็มความกว้าง, สูง 1px
     *   MAX_VALUE ใช้กับ BoxLayout เพื่อให้ขยายเต็ม
     *
     * @return JSeparator เส้นคั่นสีเทาอ่อน
     */
    private JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setForeground(UI.BORDER);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }
}
