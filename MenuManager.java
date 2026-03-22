package YummyList;

import YummyList.Interface.Manageable;

import java.io.*;
import java.util.*;

/**
 * ========================================================
 *  MenuManager — จัดการเมนูอาหารทั้งหมดในระบบ
 * ========================================================
 * implements Manageable → ต้องมี addMenu, editMenu, deleteMenu, getMenuDisplay
 *
 * เป็น "ตัวกลาง" ระหว่าง UI (AdminFrame, MemberFrame) กับข้อมูล Menu
 * เก็บ List<Menu> ไว้ใน memory และซิงค์กับไฟล์ menu.txt
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← สร้างโดย  Main.main()       → new MenuManager()
 *   → ส่งต่อไป  LoginFrame          (constructor param)
 *   → ส่งต่อไป  AdminFrame          (constructor param)
 *   → ส่งต่อไป  MemberFrame         (constructor param)
 *   → ใช้ใน     AdminFrame          → addMenu, editMenu, deleteMenu, getAllMenus, getMenuById
 *   → ใช้ใน     MemberFrame         → getAllMenus, getMenuById
 *
 * ไฟล์ที่เกี่ยวข้อง:
 *   menu.txt — เก็บเมนูอาหาร รูปแบบ "id,name,price,cookTime" ต่อบรรทัด
 */
public class MenuManager implements Manageable {

    /**
     * menus — List เก็บ Menu Object ทั้งหมดที่อยู่ใน memory
     *
     * โหลดจากไฟล์ตอน constructor
     * ถ้าไม่มีข้อมูล → เรียก addDefaults() สร้าง 12 เมนูเริ่มต้น
     */
    private final List<Menu> menus = new ArrayList<>();

    /**
     * FILE — ชื่อไฟล์เก็บเมนู (อยู่ใน working directory)
     * static final = constant ร่วมกันทั้ง class
     */
    private static final String FILE = "menu.txt";

    /**
     * Constructor — สร้าง MenuManager โหลดข้อมูลและตั้งค่าเริ่มต้น
     *
     * 1. loadFromFile() → โหลด menu.txt เข้า menus list
     * 2. ถ้า menus.isEmpty() (ไฟล์ไม่มีหรือว่าง)
     *    → addDefaults()  สร้าง 12 เมนู default
     *    → saveToFile()   บันทึกเมนู default ลงไฟล์
     *
     * เรียกจาก: Main.main() → new MenuManager()
     */
    public MenuManager() {
        loadFromFile();
        if (menus.isEmpty()) {
            addDefaults();   // สร้างเมนูตัวอย่าง 12 รายการ
            saveToFile();    // บันทึกลงไฟล์เลย
        }
    }

    // ==================== Manageable (CRUD) ====================

    /**
     * addMenu(Menu m) — เพิ่มเมนูใหม่
     * (override จาก Manageable)
     *
     * 1. getMenuById(m.getId()) → ตรวจ ID ซ้ำ
     *    ถ้าซ้ำ (ไม่ใช่ null) → return false ทันที
     * 2. menus.add(m)  → เพิ่มเข้า list
     * 3. saveToFile()  → บันทึกลงไฟล์
     *
     * ใช้ใน: AdminFrame.doAdd()
     *   boolean ok = menuManager.addMenu(new Menu(...))
     *   ok=true → success("Menu added!"), refreshTable()
     *   ok=false → error("ID already exists.")
     *
     * @param m  Menu Object ที่ต้องการเพิ่ม
     * @return true ถ้าเพิ่มสำเร็จ, false ถ้า ID ซ้ำ
     */
    @Override
    public boolean addMenu(Menu m) {
        if (getMenuById(m.getId()) != null) return false;  // ตรวจ ID ซ้ำ
        menus.add(m);
        saveToFile();
        return true;
    }

    /**
     * editMenu(int id, String name, double price, int cookTime) — แก้ไขเมนู
     * (override จาก Manageable)
     *
     * 1. getMenuById(id) → ค้นหาเมนู
     *    ถ้าไม่เจอ (null) → return false
     * 2. m.setName(), m.setPrice(), m.setCookTime() → อัปเดตข้อมูล
     * 3. saveToFile() → บันทึก
     *
     * ใช้ใน: AdminFrame.doEdit()
     *   boolean ok = menuManager.editMenu(id, name, price, time)
     *
     * @param id        ID ของเมนูที่แก้ไข
     * @param name      ชื่อใหม่
     * @param price     ราคาใหม่
     * @param cookTime  เวลาทำอาหารใหม่
     * @return true ถ้าแก้ไขสำเร็จ, false ถ้าไม่เจอ ID
     */
    @Override
    public boolean editMenu(int id, String name, double price, int cookTime) {
        Menu m = getMenuById(id);
        if (m == null) return false;
        m.setName(name);           // อัปเดตชื่อ
        m.setPrice(price);         // อัปเดตราคา
        m.setCookTime(cookTime);   // อัปเดตเวลาทำ
        saveToFile();
        return true;
    }

    /**
     * deleteMenu(int id) — ลบเมนูออกจากระบบ
     * (override จาก Manageable)
     *
     * removeIf(predicate) → ลบ element ทุกตัวที่ predicate = true
     *   m.getId() == id → ลบเมนูที่มี id ตรงกัน (ควรมีแค่ 1 ตัว)
     * ถ้าลบได้จริง (removed=true) → saveToFile()
     *
     * ใช้ใน: AdminFrame.doDelete()
     *   ถาม confirm dialog ก่อน แล้วเรียก deleteMenu(id)
     *
     * @param id  ID ของเมนูที่ต้องการลบ
     * @return true ถ้าลบสำเร็จ, false ถ้าไม่เจอ ID
     */
    @Override
    public boolean deleteMenu(int id) {
        boolean removed = menus.removeIf(m -> m.getId() == id);
        if (removed) saveToFile();
        return removed;
    }

    /**
     * getMenuDisplay() — สร้าง String แสดงตารางเมนูทั้งหมด
     * (override จาก Manageable)
     *
     * ใช้ String.format() จัดคอลัมน์ตามความกว้างที่กำหนด
     * %-4s = left-aligned, padding ให้ยาว 4 ตัวอักษร
     *
     * หมายเหตุ: ไม่ได้ใช้ใน Swing UI โดยตรง
     *   เตรียมไว้สำหรับ console mode หรือ debug
     *
     * @return String ตารางเมนู หลายบรรทัด
     */
    @Override
    public String getMenuDisplay() {
        StringBuilder sb = new StringBuilder("=========== YUMMY LIST MENU ===========\n\n");
        sb.append(String.format("%-4s %-18s %-10s %-10s\n", "ID", "Name", "Price", "Time(min)"));
        sb.append("--------------------------------------------------\n");
        menus.forEach(m -> sb.append(String.format("%-4d %-18s %-10.2f %-10d\n",
                m.getId(), m.getName(), m.getPrice(), m.getCookTime())));
        return sb.append("\nPlease enter Menu ID to order.").toString();
    }

    // ==================== Helpers ====================

    /**
     * getMenuById(int id) — ค้นหา Menu ด้วย ID
     *
     * ใช้ Stream:
     *   menus.stream().filter(m -> m.getId() == id).findFirst().orElse(null)
     *
     * ใช้ใน:
     *   - addMenu()            → ตรวจ ID ซ้ำ
     *   - editMenu()           → หาเมนูก่อนแก้ไข
     *   - AdminFrame.loadSelectedToForm()   → โหลดข้อมูลเมนูที่เลือกลง form
     *   - MemberFrame.addToCart()          → หา Menu Object จาก id ที่คลิก
     *
     * @param id  ID ที่ต้องการค้นหา
     * @return Menu ถ้าเจอ, null ถ้าไม่เจอ
     */
    public Menu getMenuById(int id) {
        return menus.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }

    /**
     * getAllMenus() — คืน List เมนูทั้งหมด
     *
     * ใช้ใน:
     *   - AdminFrame.refreshTable():
     *       menuManager.getAllMenus().forEach(m -> tableModel.addRow(...))
     *   - MemberFrame.refreshMenuTable():
     *       menuManager.getAllMenus().forEach(m -> menuModel.addRow(...))
     *
     * @return List<Menu> ทั้งหมดใน memory (reference เดิม ไม่ใช่ copy)
     */
    public List<Menu> getAllMenus() { return menus; }

    // ==================== File I/O ====================

    /**
     * saveToFile() — บันทึกเมนูทั้งหมดลงไฟล์ menu.txt (overwrite)
     *
     * FileWriter(FILE) — overwrite mode
     * PrintWriter.println(m) → m.toString() = "id,name,price,cookTime"
     * try-with-resources → auto-close (flush + close)
     *
     * เรียกจาก: addMenu, editMenu, deleteMenu, constructor (ผ่าน addDefaults)
     */
    public void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            menus.forEach(m -> pw.println(m));
        } catch (IOException e) {
            System.out.println("Error saving menus");
        }
    }

    /**
     * loadFromFile() — โหลดเมนูจากไฟล์ menu.txt เข้า menus list
     *
     * menus.clear() ก่อนโหลด → ป้องกันข้อมูลซ้ำถ้าเรียกหลายครั้ง
     *
     * แต่ละบรรทัด: split(",") → d[0..3]
     *   d[0] = id (parseInt)
     *   d[1] = name
     *   d[2] = price (parseDouble)
     *   d[3] = cookTime (parseInt) — ถ้าไม่มี d[3] ใช้ค่า default = 10
     *
     * ข้ามบรรทัดว่าง (line.isEmpty()) และบรรทัดที่ข้อมูลไม่ครบ (d.length < 3)
     *
     * เรียกจาก: constructor ครั้งเดียวตอนเริ่มโปรแกรม
     */
    public void loadFromFile() {
        menus.clear();   // ล้าง list ก่อนโหลดใหม่
        try (Scanner sc = new Scanner(new File(FILE))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;      // ข้ามบรรทัดว่าง
                String[] d = line.split(",");
                if (d.length < 3) continue;        // ข้ามบรรทัดที่ข้อมูลไม่ครบ
                menus.add(new Menu(
                        Integer.parseInt(d[0].trim()),       // id
                        d[1].trim(),                          // name
                        Double.parseDouble(d[2].trim()),      // price
                        d.length >= 4 ? Integer.parseInt(d[3].trim()) : 10  // cookTime หรือ default 10
                ));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Menu file not found");
        }
    }

    /**
     * addDefaults() — สร้าง 12 เมนูตัวอย่างเมื่อไม่มีข้อมูลเริ่มต้น
     *
     * เรียกจาก constructor ถ้า menus.isEmpty() หลัง loadFromFile()
     *
     * data[][] — array 2 มิติ: {{id, price, cookTime}, ...}
     * names[]  — array ชื่อเมนู ลำดับตรงกับ data[][]
     *
     * วนลูป 12 ครั้ง สร้าง new Menu แต่ละตัวแล้ว add เข้า menus list
     * (ยังไม่บันทึกไฟล์ในนี้ — constructor เรียก saveToFile() หลังจากนี้)
     */
    private void addDefaults() {
        // {id, price, cookTime} สำหรับแต่ละเมนู
        int[][] data = {
            {1,50,10},{2,60,12},{3,80,15},{4,70,18},{5,40,8},
            {6,90,20},{7,85,14},{8,120,25},{9,100,16},{10,150,30},{11,35,5},{12,45,3}
        };
        // ชื่อเมนู ลำดับตรงกับ data[][]
        String[] names = {
            "Fried Rice","Pad Thai","Tom Yum","Green Curry","Som Tam",
            "Grilled Chicken","Burger","Pizza","Spaghetti","Steak","Ice Cream","Coffee"
        };
        for (int i = 0; i < names.length; i++)
            menus.add(new Menu(data[i][0], names[i], data[i][1], data[i][2]));
    }
}
