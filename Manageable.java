package YummyList.Interface;

import YummyList.Menu;

/**
 * ========================================================
 *  Manageable — Interface สำหรับจัดการเมนูอาหาร (CRUD)
 * ========================================================
 * กำหนด "สัญญา" CRUD (Create/Read/Update/Delete) ของเมนูอาหาร
 * class ที่ implements ต้องมีความสามารถครบทั้ง 4 อย่าง
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← MenuManager.java  implements Manageable
 *                       (MenuManager คือผู้จัดการเมนูจริงในระบบ)
 *
 *   ใช้งานผ่าน:
 *   - AdminFrame  → เรียก addMenu(), editMenu(), deleteMenu()
 *                   เมื่อ Admin กดปุ่มใน form
 *   - AdminFrame  → เรียก getAllMenus() (ผ่าน MenuManager โดยตรง)
 *                   เพื่อแสดงตารางเมนู
 */
public interface Manageable {

    /**
     * addMenu(Menu menu) — เพิ่มเมนูใหม่เข้าระบบ
     *
     * ตรวจสอบ: ถ้า ID ซ้ำกับที่มีอยู่แล้ว → return false
     * ถ้าเพิ่มสำเร็จ → บันทึกลงไฟล์ menu.txt ทันที
     *
     * ใช้ใน:
     *   - AdminFrame.doAdd():
     *       สร้าง new Menu(...) จาก form แล้วส่งเข้า addMenu()
     *       ถ้า return false → แสดง error "ID already exists."
     *
     * @param menu  Object Menu ที่ต้องการเพิ่ม
     * @return true ถ้าเพิ่มสำเร็จ, false ถ้า ID ซ้ำ
     */
    boolean addMenu(Menu menu);

    /**
     * editMenu(int id, String name, double price, int cookTime)
     * — แก้ไขข้อมูลเมนูที่มี ID ตรงกัน
     *
     * ค้นหาเมนูด้วย id → ถ้าไม่เจอ return false
     * ถ้าเจอ → อัปเดต name, price, cookTime แล้วบันทึกไฟล์
     *
     * ใช้ใน:
     *   - AdminFrame.doEdit():
     *       อ่านค่าจาก form แล้วเรียก editMenu()
     *       ถ้า return false → แสดง error "Menu ID X not found."
     *
     * @param id        ID ของเมนูที่ต้องการแก้ไข
     * @param name      ชื่อเมนูใหม่
     * @param price     ราคาใหม่ (บาท)
     * @param cookTime  เวลาทำอาหารใหม่ (นาที)
     * @return true ถ้าแก้ไขสำเร็จ, false ถ้าไม่เจอ ID
     */
    boolean editMenu(int id, String name, double price, int cookTime);

    /**
     * deleteMenu(int id) — ลบเมนูที่มี ID ตรงกันออกจากระบบ
     *
     * ค้นหาและลบด้วย removeIf() → ถ้าลบได้ก็บันทึกไฟล์ใหม่
     *
     * ใช้ใน:
     *   - AdminFrame.doDelete():
     *       ขอการยืนยัน JOptionPane ก่อน แล้วเรียก deleteMenu()
     *       ถ้า return false → แสดง error "Menu ID X not found."
     *
     * @param id  ID ของเมนูที่ต้องการลบ
     * @return true ถ้าลบสำเร็จ, false ถ้าไม่เจอ ID
     */
    boolean deleteMenu(int id);

    /**
     * getMenuDisplay() — สร้าง String แสดงเมนูทั้งหมดในรูปแบบ text table
     *
     * รูปแบบ output:
     *   =========== YUMMY LIST MENU ===========
     *   ID   Name               Price      Time(min)
     *   --------------------------------------------------
     *   1    Fried Rice         50.00      10
     *   ...
     *   Please enter Menu ID to order.
     *
     * หมายเหตุ: method นี้ไม่ได้ถูกเรียกใช้โดย UI (Swing) โดยตรงในโปรแกรมนี้
     * แต่เตรียมไว้เผื่อใช้ใน console mode หรือ debug
     *
     * @return String หลายบรรทัดแสดงตารางเมนู
     */
    String getMenuDisplay();
}
