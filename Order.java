package YummyList;

import YummyList.Interface.Orderable;

import java.util.ArrayList;

/**
 * ========================================================
 *  Order — เก็บรายการสั่งอาหารทั้งหมดใน 1 session การ Checkout
 * ========================================================
 * implements Orderable → ต้องมี addItem, calculateTotal,
 *                         calculateCookTime, getOrderSummary
 *
 * ใช้ Java 16+ Record feature (Item record) แทน inner class
 * เพื่อความกระชับ ไม่ต้องเขียน constructor/getter เอง
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← สร้างโดย MemberFrame.doCheckout(): new Order()
 *              แล้ววนเพิ่ม order.addItem(menu, qty) ทุกรายการในตะกร้า
 *   → ส่งต่อไป ReceiptFrame (constructor) เพื่อแสดงใบเสร็จ
 *   → ใช้ใน    MessageLogger.save() เพื่อบันทึกลงไฟล์
 *
 * lifecycle:
 *   MemberFrame (ตะกร้า) → [checkout] → Order → ReceiptFrame → ทิ้ง
 */
public class Order implements Orderable {

    /**
     * Item — Record เก็บ 1 รายการในออเดอร์
     *
     * Java record (Java 16+):
     *   - constructor ถูกสร้างอัตโนมัติ: new Item(menu, qty)
     *   - getter ถูกสร้างอัตโนมัติ: item.menu(), item.qty()
     *   - immutable (final fields)
     *
     * แทนที่จะเขียน inner class แบบเต็ม ประหยัด code ได้มาก
     *
     * @param menu  Object Menu ของรายการนั้น (ข้อมูลชื่อ/ราคา/เวลา)
     * @param qty   จำนวนที่สั่ง (int)
     */
    private record Item(Menu menu, int qty) {}

    /**
     * items — List เก็บ Item ทั้งหมดในออเดอร์นี้
     *
     * เพิ่มด้วย addItem(), อ่านด้วย stream ใน calculateTotal/CookTime/Summary
     * ArrayList เพราะต้องการ random access และ iteration
     */
    private final ArrayList<Item> items = new ArrayList<>();

    /**
     * addItem(Menu menu, int qty) — เพิ่มรายการอาหารลงออเดอร์
     * (override จาก Orderable)
     *
     * สร้าง new Item(menu, qty) แล้ว add ลง items list
     * ไม่ merge รายการซ้ำ — การ merge ทำใน MemberFrame.addToCart() แล้ว
     *
     * ใช้ใน:
     *   - MemberFrame.doCheckout():
     *       for (int i = 0; i < cartMenus.size(); i++)
     *           order.addItem(cartMenus.get(i), cartQtys.get(i))
     *
     * @param menu  Object Menu ที่สั่ง
     * @param qty   จำนวน (ควร >= 1)
     */
    @Override
    public void addItem(Menu menu, int qty) { items.add(new Item(menu, qty)); }

    /**
     * calculateTotal() — คำนวณราคารวมทั้งออเดอร์
     * (override จาก Orderable)
     *
     * ใช้ Stream API:
     *   items.stream()
     *     .mapToDouble(i -> i.menu().getPrice() * i.qty())
     *     .sum()
     *
     * mapToDouble() → แปลงแต่ละ Item เป็น double (price × qty)
     * sum()         → รวมทุกค่าเป็นผลรวมเดียว
     *
     * ใช้ใน:
     *   - MemberFrame.doCheckout():
     *       double total = order.calculateTotal()
     *       → ส่งไป showPointsDialog (แสดง "Order Total")
     *       → นำมาคำนวณ total -= usePoint
     *       → ส่งไป ReceiptFrame
     *
     * @return ราคารวมทั้งหมด (double, บาท)
     */
    @Override
    public double calculateTotal() {
        return items.stream().mapToDouble(i -> i.menu().getPrice() * i.qty()).sum();
    }

    /**
     * calculateCookTime() — คำนวณเวลาทำอาหารรวม
     * (override จาก Orderable)
     *
     * ใช้ Stream API:
     *   items.stream()
     *     .mapToInt(i -> i.menu().getCookTime() * i.qty())
     *     .sum()
     *
     * สมมติทำทีละรายการต่อกัน (ไม่ได้ทำพร้อมกัน)
     * ดังนั้นเวลารวม = Σ (cookTime × qty) ของทุกรายการ
     *
     * ใช้ใน:
     *   - MemberFrame.doCheckout():
     *       ส่งเป็น param ไป new ReceiptFrame(..., order.calculateCookTime(), ...)
     *   - ReceiptFrame: แสดง "Cook Time: X min"
     *   - MessageLogger.save(): บันทึกในไฟล์ message.txt
     *
     * @return เวลาทำรวม (int, นาที)
     */
    @Override
    public int calculateCookTime() {
        return items.stream().mapToInt(i -> i.menu().getCookTime() * i.qty()).sum();
    }

    /**
     * getOrderSummary() — สร้าง String สรุปรายการอาหารทั้งหมด
     * (override จาก Orderable)
     *
     * รูปแบบ output:
     *   ========= ORDER LIST =========
     *   Fried Rice x 2 = 100.0
     *   Pad Thai x 1 = 60.0
     *   ===============================
     *
     * StringBuilder ใช้เพราะ String concatenation ใน loop ไม่มีประสิทธิภาพ
     * (.append() ดีกว่า += ใน loop)
     *
     * ใช้ใน:
     *   - ReceiptFrame constructor:
     *       order.getOrderSummary()
     *         .replace("========= ORDER LIST =========\n", "")
     *         .replace("===============================", "").trim()
     *       → แสดงใน JTextArea หลังตัด header/footer ออก
     *   - MessageLogger.save():
     *       "========== RECEIPT ==========\n" + order.getOrderSummary() + ...
     *       → บันทึกลง message.txt แบบ append
     *
     * @return String หลายบรรทัดสรุปออเดอร์
     */
    @Override
    public String getOrderSummary() {
        StringBuilder sb = new StringBuilder("========= ORDER LIST =========\n");
        for (Item i : items)
            sb.append(i.menu().getName())       // ชื่อเมนู
              .append(" x ")
              .append(i.qty())                  // จำนวน
              .append(" = ")
              .append(i.menu().getPrice() * i.qty())  // ราคา × จำนวน
              .append("\n");
        return sb.append("===============================").toString();
    }
}
