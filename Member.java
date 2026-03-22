package YummyList;

import YummyList.Interface.Pointable;

/**
 * ========================================================
 *  Member — เก็บข้อมูลสมาชิก 1 คน
 * ========================================================
 * เป็น Data Model class (Plain Old Java Object — POJO)
 * เก็บสถานะ: username, password (plain text), และแต้มสะสม (points)
 *
 * implements Pointable:
 *   → ต้องมี getPoints() และ addPoints() ตามที่ Interface กำหนด
 *   → ทำให้ MemberFrame สามารถเรียกผ่าน type Pointable ได้
 *      (แม้ในโปรแกรมนี้จะเรียกผ่าน Member โดยตรง)
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← สร้างโดย  MemberManager (ตอน register หรือ loadFromFile)
 *   → ใช้ใน     LoginFrame.doMemberLogin()   → รับ Member คืนจาก login()
 *   → ใช้ใน     MemberFrame  (constructor)   → ถือ Member ตลอด session
 *   → ใช้ใน     MemberFrame.doCheckout()     → อ่าน/แก้ไข points
 *   → ใช้ใน     ReceiptFrame (constructor)   → แสดง points บนใบเสร็จ
 */
public class Member implements Pointable {

    /*
     * ฟิลด์ข้อมูลสมาชิก
     * username, password เป็น final → ไม่เปลี่ยนหลังสร้าง
     * points ไม่ใช่ final → เปลี่ยนได้เมื่อใช้/ได้รับแต้ม
     */

    /** ชื่อผู้ใช้ — ใช้เป็น key ในการค้นหาและแสดงผล */
    private final String username;

    /** รหัสผ่าน — เก็บแบบ plain text (ไม่ได้ hash ในโปรแกรมนี้) */
    private final String password;

    /** แต้มสะสม — เพิ่มได้จาก checkout, ใช้ได้ตอน checkout */
    private int points;

    /**
     * Constructor — สร้าง Member พร้อมข้อมูลครบ
     *
     * เรียกจาก 2 ที่:
     *   1. MemberManager.register() → new Member(user, pass, 0)
     *      (สมาชิกใหม่เริ่มต้นที่ 0 แต้ม)
     *   2. MemberManager.loadFromFile() → new Member(d[0], d[1], parseInt(d[2]))
     *      (โหลดจากไฟล์ — แต้มอาจไม่ใช่ 0)
     *
     * @param username  ชื่อผู้ใช้ (String)
     * @param password  รหัสผ่าน (String, plain text)
     * @param points    แต้มสะสมเริ่มต้น (int)
     */
    public Member(String username, String password, int points) {
        this.username = username;
        this.password = password;
        this.points   = points;
    }

    /**
     * getUsername() — คืนชื่อผู้ใช้
     *
     * ใช้ใน:
     *   - MemberFrame constructor: setTitle("YummyList — " + member.getUsername())
     *   - MemberManager.login(): stream filter เปรียบเทียบ username
     *   - MemberManager.saveToFile(): ผ่าน toString()
     */
    public String getUsername() { return username; }

    /**
     * checkPassword(String pass) — ตรวจสอบว่ารหัสผ่านตรงกันหรือไม่
     *
     * เปรียบเทียบด้วย .equals() (case-sensitive)
     *
     * ใช้ใน:
     *   - MemberManager.login():
     *       stream filter → m.checkPassword(pass)
     *       ถ้า true → คืน Member นั้น
     *
     * @param pass  รหัสผ่านที่ต้องการตรวจสอบ
     * @return true ถ้าตรงกัน, false ถ้าไม่ตรง
     */
    public boolean checkPassword(String pass) { return password.equals(pass); }

    /**
     * getPoints() — คืนแต้มสะสมปัจจุบัน
     * (override จาก Interface Pointable)
     *
     * ใช้ใน:
     *   - MemberFrame.buildTopBar()    → "⭐ X pts"
     *   - MemberFrame.buildSummary()   → "⭐ X"
     *   - MemberFrame.showPointsDialog() → แสดง "Your Points: X pts"
     *   - MemberFrame.doCheckout()     → ตรวจว่า usePoint > getPoints()
     *   - ReceiptFrame                 → "Total Points: ⭐ X pts"
     */
    @Override
    public int getPoints() { return points; }

    /**
     * addPoints(int amount) — เพิ่ม (หรือหัก) แต้มสะสม
     * (override จาก Interface Pointable)
     *
     * points += amount
     *   ถ้า amount > 0 → ได้รับแต้ม (earned points 5%)
     *   ถ้า amount < 0 → ใช้แต้ม    (หักออกจากยอดที่ต้องจ่าย)
     *
     * ใช้ใน MemberFrame.doCheckout():
     *   member.addPoints(-usePoint)  → หักแต้มที่เลือกใช้
     *   member.addPoints(earned)     → บวกแต้มที่ได้จากการซื้อ (5% ของยอดสุทธิ)
     *
     * @param amount จำนวนแต้มที่จะเพิ่ม (ลบได้ = หักแต้ม)
     */
    @Override
    public void addPoints(int amount) { points += amount; }

    /**
     * toString() — แปลง Member เป็น String รูปแบบ CSV
     *
     * รูปแบบ: "username,password,points"
     * ตัวอย่าง: "alice,pass123,150"
     *
     * ใช้ใน:
     *   - MemberManager.saveToFile():
     *       members.forEach(m -> pw.println(m))
     *       → Java เรียก m.toString() โดยอัตโนมัติ
     *       → เขียนแต่ละ Member เป็น 1 บรรทัดในไฟล์ member.txt
     */
    @Override
    public String toString() { return username + "," + password + "," + points; }
}
