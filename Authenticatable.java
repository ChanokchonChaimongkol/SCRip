package YummyList.Interface;

import YummyList.Member;

/**
 * ========================================================
 *  Authenticatable — Interface สำหรับระบบ สมัครสมาชิก / เข้าสู่ระบบ
 * ========================================================
 * กำหนด "สัญญา" ของ class ที่ดูแลเรื่อง Authentication
 * ต้องสามารถสมัคร, login, บันทึก และโหลดข้อมูลสมาชิกได้
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← MemberManager.java  implements Authenticatable
 *                         (MemberManager คือผู้จัดการสมาชิกจริงในระบบ)
 *
 *   ใช้งานผ่าน:
 *   - LoginFrame  → เรียก register() เมื่อกด "Create Account"
 *   - LoginFrame  → เรียก login() เมื่อกด "Login"
 *   - MemberFrame → เรียก saveToFile() หลัง checkout เพื่อบันทึกแต้มใหม่
 *   - MemberManager constructor → เรียก loadFromFile() ตอนเริ่มโปรแกรม
 */
public interface Authenticatable {

    /**
     * register(String user, String pass) — สมัครสมาชิกใหม่
     *
     * สร้าง Member ใหม่ด้วย points = 0 แล้วเพิ่มเข้า list
     * และบันทึกลงไฟล์ member.txt ทันที
     *
     * หมายเหตุ: การตรวจสอบ username ซ้ำทำใน LoginFrame.doRegister()
     * (อ่านไฟล์โดยตรงด้วย Scanner ก่อนเรียก register())
     *
     * ใช้ใน:
     *   - LoginFrame.doRegister():
     *       ตรวจ username ซ้ำ, password match แล้วเรียก memberManager.register()
     *
     * @param user  ชื่อผู้ใช้ที่ต้องการสมัคร
     * @param pass  รหัสผ่าน (เก็บเป็น plain text ในไฟล์)
     */
    void register(String user, String pass);

    /**
     * login(String user, String pass) — ตรวจสอบและเข้าสู่ระบบ
     *
     * ค้นหาใน list ด้วย stream filter:
     *   username ตรงกัน AND password ตรงกัน → คืน Member นั้น
     *   ถ้าไม่เจอ → คืน null
     *
     * ใช้ใน:
     *   - LoginFrame.doMemberLogin():
     *       Member m = memberManager.login(user, pass)
     *       ถ้า m != null → เปิด MemberFrame(m, ...)
     *       ถ้า m == null → แสดง error "Invalid username or password."
     *
     * @param user  ชื่อผู้ใช้
     * @param pass  รหัสผ่าน
     * @return Member object ถ้า login สำเร็จ, null ถ้าไม่สำเร็จ
     */
    Member login(String user, String pass);

    /**
     * saveToFile() — บันทึกข้อมูลสมาชิกทั้งหมดลงไฟล์ member.txt
     *
     * เขียนทับไฟล์ทั้งหมด (overwrite ไม่ใช่ append)
     * แต่ละบรรทัด = Member.toString() = "username,password,points"
     *
     * ใช้ใน:
     *   - MemberManager.register() → บันทึกหลังสมัครสมาชิกใหม่
     *   - MemberFrame.doCheckout() → บันทึกหลังแต้มเปลี่ยนแปลง
     *       memberManager.saveToFile() เรียกทันทีหลัง addPoints()
     */
    void saveToFile();

    /**
     * loadFromFile() — โหลดข้อมูลสมาชิกจากไฟล์ member.txt เข้า List
     *
     * แต่ละบรรทัดในไฟล์ format: "username,password,points"
     * split(",") แล้วสร้าง new Member(d[0], d[1], Integer.parseInt(d[2]))
     *
     * ใช้ใน:
     *   - MemberManager constructor: new MemberManager() → loadFromFile()
     *     เรียกครั้งเดียวตอนเริ่มโปรแกรมใน Main.main()
     *
     * ถ้าไฟล์ไม่มี → print "Member file not found" (ไม่ crash)
     */
    void loadFromFile();
}
