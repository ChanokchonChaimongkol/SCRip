package YummyList.Interface;

import YummyList.Menu;

/**
 * ========================================================
 *  Orderable — Interface สำหรับจัดการรายการสั่งอาหาร
 * ========================================================
 * กำหนด "สัญญา" สำหรับ class ที่ทำหน้าที่เป็น "ออเดอร์อาหาร"
 * ต้องสามารถเพิ่มรายการ, คำนวณราคารวม, คำนวณเวลาทำ, และสรุปออเดอร์ได้
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← Order.java  implements Orderable
 *                 (Order คือ class ที่เก็บรายการอาหารใน 1 session การสั่ง)
 *
 *   ใช้งานผ่าน:
 *   - MemberFrame   → สร้าง new Order() แล้วเรียก addItem(), calculateTotal(),
 *                      calculateCookTime() ตอน checkout
 *   - ReceiptFrame  → เรียก getOrderSummary() เพื่อแสดงรายการบนใบเสร็จ
 */
public interface Orderable {

    /**
     * addItem(Menu menu, int quantity) — เพิ่มเมนูอาหารพร้อมจำนวนลงในออเดอร์
     *
     * ใช้ใน:
     *   - MemberFrame.doCheckout():
     *       วนลูปผ่าน cartMenus + cartQtys แล้วเรียก order.addItem(menu, qty)
     *       เพื่อสร้างออเดอร์ก่อนส่งต่อไปคำนวณราคา
     *
     * @param menu     Object เมนูอาหาร (มาจาก MenuManager)
     * @param quantity จำนวนที่สั่ง (>= 1)
     */
    void addItem(Menu menu, int quantity);

    /**
     * calculateTotal() — คำนวณราคารวมทั้งออเดอร์ (ก่อนหักแต้ม)
     *
     * สูตร: Σ (price ของแต่ละเมนู × จำนวนที่สั่ง)
     *
     * ใช้ใน:
     *   - MemberFrame.doCheckout():
     *       double total = order.calculateTotal()
     *       → นำไปแสดงใน showPointsDialog และหักแต้ม
     *
     * @return ราคารวมทั้งหมด (double, หน่วย: บาท)
     */
    double calculateTotal();

    /**
     * calculateCookTime() — คำนวณเวลาทำอาหารรวมทั้งออเดอร์
     *
     * สูตร: Σ (cookTime ของแต่ละเมนู × จำนวนที่สั่ง)
     * (สมมติทำทีละรายการต่อกัน ไม่ได้ทำพร้อมกัน)
     *
     * ใช้ใน:
     *   - MemberFrame.doCheckout():
     *       ส่งค่าไปแสดงที่ ReceiptFrame (cookTime param)
     *   - ReceiptFrame → แสดง "Cook Time: X min"
     *
     * @return เวลาทำอาหารรวม (int, หน่วย: นาที)
     */
    int calculateCookTime();

    /**
     * getOrderSummary() — สร้าง String สรุปรายการอาหารทั้งหมด
     *
     * รูปแบบ output:
     *   ========= ORDER LIST =========
     *   <ชื่อเมนู> x <จำนวน> = <ราคารวมรายการ>
     *   ...
     *   ===============================
     *
     * ใช้ใน:
     *   - ReceiptFrame → แสดงรายการใน JTextArea บนใบเสร็จ
     *   - MessageLogger.save() → บันทึกลงไฟล์ message.txt
     *
     * @return String หลายบรรทัดสรุปออเดอร์ทั้งหมด
     */
    String getOrderSummary();
}
