package YummyList;

/**
 * ========================================================
 *  Menu — เก็บข้อมูลเมนูอาหาร 1 รายการ
 * ========================================================
 * เป็น Data Model class (POJO) ไม่มี logic ซับซ้อน
 * เก็บสถานะ: id, name, price, cookTime
 *
 * id เป็น final → ไม่เปลี่ยนหลังสร้าง (ใช้เป็น primary key)
 * name, price, cookTime เปลี่ยนได้ผ่าน setter (ใช้ตอน Admin edit)
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← สร้างโดย  MenuManager (ตอน loadFromFile หรือ addDefaults)
 *   ← สร้างโดย  AdminFrame.doAdd() → new Menu(id, name, price, time)
 *   → ใช้ใน     MenuManager   (เก็บใน List<Menu>)
 *   → ใช้ใน     Order         (เก็บใน record Item เพื่อคำนวณ)
 *   → ใช้ใน     MemberFrame   (แสดงตาราง, เพิ่มลงตะกร้า)
 *   → ใช้ใน     Orderable, Manageable interfaces (พารามิเตอร์)
 */
public class Menu {

    /** ID เมนู — ใช้เป็น primary key ค้นหาและอ้างอิง (final ห้ามเปลี่ยน) */
    private final int id;

    /** ชื่อเมนูอาหาร — แก้ไขได้โดย Admin */
    private String name;

    /** ราคา (บาท) — แก้ไขได้โดย Admin */
    private double price;

    /** เวลาทำอาหาร (นาที) — แก้ไขได้โดย Admin */
    private int cookTime;

    /**
     * Constructor — สร้างเมนูพร้อมข้อมูลครบ
     *
     * เรียกจาก 3 ที่:
     *   1. MenuManager.addDefaults()   → สร้าง 12 เมนู default
     *   2. MenuManager.loadFromFile()  → โหลดจากไฟล์ menu.txt
     *   3. AdminFrame.doAdd()          → Admin สร้างเมนูใหม่ผ่าน form
     *
     * @param id        รหัสเมนู (int, unique)
     * @param name      ชื่อเมนู (String)
     * @param price     ราคา (double, หน่วย: บาท)
     * @param cookTime  เวลาทำ (int, หน่วย: นาที)
     */
    public Menu(int id, String name, double price, int cookTime) {
        this.id       = id;
        this.name     = name;
        this.price    = price;
        this.cookTime = cookTime;
    }

    // ==================== Getters ====================

    /**
     * getId() — คืน ID เมนู
     * ใช้ใน:
     *   - MenuManager.getMenuById(): stream filter m.getId() == id
     *   - MenuManager.deleteMenu(): removeIf m.getId() == id
     *   - AdminFrame.loadSelectedToForm(): อ่าน id จากตารางแล้วค้นหา
     *   - MemberFrame.addToCart(): เปรียบเทียบหาเมนูซ้ำในตะกร้า
     *   - toString(): ใส่ใน CSV
     */
    public int getId() { return id; }

    /**
     * getName() — คืนชื่อเมนู
     * ใช้ใน:
     *   - AdminFrame.refreshTable(): tableModel.addRow → m.getName()
     *   - MemberFrame.refreshMenuTable(): เช่นเดียวกัน
     *   - Order.getOrderSummary(): ชื่อเมนูในสรุปออเดอร์
     *   - toString(): ใส่ใน CSV
     */
    public String getName() { return name; }

    /**
     * getPrice() — คืนราคา (บาท)
     * ใช้ใน:
     *   - AdminFrame.refreshTable(): แสดงในตาราง
     *   - MemberFrame: คำนวณ subtotal ในตะกร้า
     *   - Order.calculateTotal(): Σ price × qty
     *   - Order.getOrderSummary(): แสดงราคาต่อรายการ
     *   - toString(): ใส่ใน CSV
     */
    public double getPrice() { return price; }

    /**
     * getCookTime() — คืนเวลาทำอาหาร (นาที)
     * ใช้ใน:
     *   - AdminFrame.refreshTable(): แสดงในตาราง
     *   - MemberFrame.refreshSummary(): คำนวณเวลารวมในตะกร้า
     *   - Order.calculateCookTime(): Σ cookTime × qty
     *   - toString(): ใส่ใน CSV
     */
    public int getCookTime() { return cookTime; }

    // ==================== Setters ====================
    // ใช้เฉพาะตอน Admin กด "Save Edit" ใน AdminFrame.doEdit()
    // → เรียกผ่าน MenuManager.editMenu() ซึ่งเรียก m.setName(), m.setPrice(), m.setCookTime()

    /**
     * setName(String name) — เปลี่ยนชื่อเมนู
     * เรียกจาก MenuManager.editMenu() เท่านั้น
     */
    public void setName(String name) { this.name = name; }

    /**
     * setPrice(double price) — เปลี่ยนราคา
     * เรียกจาก MenuManager.editMenu() เท่านั้น
     */
    public void setPrice(double price) { this.price = price; }

    /**
     * setCookTime(int cookTime) — เปลี่ยนเวลาทำอาหาร
     * เรียกจาก MenuManager.editMenu() เท่านั้น
     */
    public void setCookTime(int cookTime) { this.cookTime = cookTime; }

    /**
     * toString() — แปลง Menu เป็น String รูปแบบ CSV
     *
     * รูปแบบ: "id,name,price,cookTime"
     * ตัวอย่าง: "1,Fried Rice,50.0,10"
     *
     * ใช้ใน:
     *   - MenuManager.saveToFile():
     *       menus.forEach(m -> pw.println(m))
     *       → Java เรียก m.toString() อัตโนมัติ
     *       → เขียนแต่ละเมนูเป็น 1 บรรทัดในไฟล์ menu.txt
     */
    @Override
    public String toString() { return id + "," + name + "," + price + "," + cookTime; }
}
