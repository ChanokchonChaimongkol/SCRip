package YummyList;

import YummyList.Interface.Authenticatable;

import java.io.*;
import java.util.*;

/**
 * ========================================================
 *  MemberManager — จัดการสมาชิกทั้งหมดในระบบ
 * ========================================================
 * implements Authenticatable → ต้องมี register, login, saveToFile, loadFromFile
 *
 * เป็น "ตัวกลาง" ระหว่าง UI (LoginFrame, MemberFrame) กับข้อมูล Member
 * เก็บ List<Member> ไว้ใน memory และซิงค์กับไฟล์ member.txt
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← สร้างโดย  Main.main()         → new MemberManager()
 *   → ส่งต่อไป  LoginFrame           (constructor param)
 *   → ส่งต่อไป  MemberFrame          (constructor param)
 *   → ส่งต่อไป  AdminFrame           (constructor param)
 *   → ใช้ใน     LoginFrame.doMemberLogin()  → login()
 *   → ใช้ใน     LoginFrame.doRegister()     → register()
 *   → ใช้ใน     MemberFrame.doCheckout()    → saveToFile()
 *
 * ไฟล์ที่เกี่ยวข้อง:
 *   member.txt — เก็บข้อมูลสมาชิก รูปแบบ "username,password,points" ต่อบรรทัด
 */
public class MemberManager implements Authenticatable {

    /**
     * members — List เก็บ Member Object ทั้งหมดที่อยู่ใน memory
     *
     * โหลดจากไฟล์ตอน constructor, เพิ่มได้ผ่าน register()
     * เป็น ArrayList เพราะต้องการ iteration และ stream
     */
    private final List<Member> members = new ArrayList<>();

    /**
     * FILE — ชื่อไฟล์เก็บข้อมูลสมาชิก (อยู่ใน working directory)
     *
     * static final = constant ไม่เปลี่ยนแปลง, ใช้ร่วมกันทั้ง class
     */
    private static final String FILE = "member.txt";

    /**
     * save(String message) — static method เพิ่มข้อความต่อท้ายไฟล์ member.txt
     *
     * *** หมายเหตุ: method นี้ใช้สำหรับ append ข้อความธรรมดาลงไฟล์
     *     ไม่เกี่ยวกับการบันทึกข้อมูล Member (ใช้ saveToFile() แทน)
     *
     * เรียกจาก LoginFrame.doRegister():
     *   ถ้าไม่มีไฟล์ member.txt (FileNotFoundException) จะเรียก MemberManager.save("")
     *   เพื่อสร้างไฟล์เปล่าก่อน แล้วค่อย register ต่อ
     *
     * FileWriter(FILE, true) — true = append mode (ไม่เขียนทับ)
     * PrintWriter — wrap FileWriter เพื่อใช้ println()
     * try-with-resources — ปิด stream อัตโนมัติเมื่อ block จบ
     *
     * @param message ข้อความที่ต้องการเพิ่มต่อท้ายไฟล์
     */
    public static void save(String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE, true))) {
            pw.println(message);   // เขียนข้อความ + newline
            pw.println("");        // เพิ่มบรรทัดว่างคั่น
        } catch (IOException e) {
            System.out.println("Error saving message: " + e.getMessage());
        }
    }

    /**
     * Constructor — สร้าง MemberManager และโหลดข้อมูลจากไฟล์ทันที
     *
     * เรียก loadFromFile() เพื่อ populate members list จาก member.txt
     * ถ้าไม่มีไฟล์ → members list จะว่าง (print ข้อความแต่ไม่ crash)
     *
     * เรียกจาก: Main.main() → new MemberManager()
     */
    public MemberManager() { loadFromFile(); }

    /**
     * register(String user, String pass) — สมัครสมาชิกใหม่
     * (override จาก Authenticatable)
     *
     * 1. สร้าง new Member(user, pass, 0)  — แต้มเริ่มต้น = 0
     * 2. เพิ่มเข้า members list
     * 3. เรียก saveToFile() บันทึกทันที
     *
     * หมายเหตุ: ไม่มีการตรวจ username ซ้ำในนี้
     *   การตรวจทำใน LoginFrame.doRegister() ก่อนเรียก register()
     *
     * ใช้ใน: LoginFrame.doRegister() → memberManager.register(user, pass)
     *
     * @param user ชื่อผู้ใช้ใหม่
     * @param pass รหัสผ่าน
     */
    @Override
    public void register(String user, String pass) {
        members.add(new Member(user, pass, 0));  // points = 0 เสมอสำหรับสมาชิกใหม่
        saveToFile();
    }

    /**
     * login(String user, String pass) — ค้นหาและยืนยันตัวตน
     * (override จาก Authenticatable)
     *
     * ใช้ Stream API:
     *   members.stream()
     *     .filter(m -> username ตรง AND password ตรง)
     *     .findFirst()   → Optional<Member>
     *     .orElse(null)  → null ถ้าไม่เจอ
     *
     * ใช้ใน: LoginFrame.doMemberLogin()
     *   Member m = memberManager.login(user, pass)
     *   m != null → เปิด MemberFrame
     *   m == null → แสดง error
     *
     * @param user ชื่อผู้ใช้
     * @param pass รหัสผ่าน
     * @return Member ถ้าพบและ password ตรง, null ถ้าไม่พบหรือ password ผิด
     */
    @Override
    public Member login(String user, String pass) {
        return members.stream()
                .filter(m -> m.getUsername().equals(user) && m.checkPassword(pass))
                .findFirst()
                .orElse(null);
    }

    /**
     * saveToFile() — บันทึกสมาชิกทั้งหมดลงไฟล์ member.txt (overwrite)
     * (override จาก Authenticatable)
     *
     * FileWriter(FILE) — ไม่มี true = overwrite mode (เขียนทับทั้งไฟล์)
     * PrintWriter.println(m) — เรียก m.toString() อัตโนมัติ
     * รูปแบบแต่ละบรรทัด: "username,password,points"
     *
     * try-with-resources — ปิด PrintWriter อัตโนมัติ (flush + close)
     *
     * ใช้ใน:
     *   - register()                    → บันทึกหลังเพิ่มสมาชิก
     *   - MemberFrame.doCheckout()      → บันทึกหลังแต้มเปลี่ยน
     */
    @Override
    public void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            // forEach วนทุก Member แล้ว println → เรียก toString() ของแต่ละตัว
            members.forEach(m -> pw.println(m));
        } catch (IOException e) {
            System.out.println("Error saving members");
        }
    }

    /**
     * loadFromFile() — โหลดข้อมูลสมาชิกจากไฟล์ member.txt เข้า members list
     * (override จาก Authenticatable)
     *
     * Scanner อ่านทีละบรรทัด → split(",") → สร้าง new Member
     *   d[0] = username
     *   d[1] = password
     *   d[2] = points (parseInt)
     *
     * try-with-resources — ปิด Scanner อัตโนมัติ
     * Exception กว้าง (Exception e) ครอบคลุม:
     *   - FileNotFoundException  → ไม่มีไฟล์
     *   - NumberFormatException  → points ไม่ใช่ตัวเลข
     *   - ArrayIndexOutOfBoundsException → บรรทัดข้อมูลไม่ครบ
     *
     * ใช้ใน: constructor MemberManager() ครั้งเดียวตอนเริ่มโปรแกรม
     */
    @Override
    public void loadFromFile() {
        try (Scanner sc = new Scanner(new File(FILE))) {
            while (sc.hasNextLine()) {
                String[] d = sc.nextLine().split(",");   // แยกด้วย comma
                // d[0]=username, d[1]=password, d[2]=points
                members.add(new Member(d[0], d[1], Integer.parseInt(d[2])));
            }
        } catch (Exception e) {
            System.out.println("Member file not found");
        }
    }
}
