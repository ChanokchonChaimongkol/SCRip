package YummyList;

import java.io.*;

/**
 * ========================================================
 *  MessageLogger — บันทึกข้อมูล Receipt ลงไฟล์ message.txt
 * ========================================================
 * Utility class ที่มีแค่ static method เดียว
 * ทำหน้าที่ append (ต่อท้าย) ข้อความลงไฟล์ message.txt
 * ไม่มี instance state — ไม่ต้อง new MessageLogger()
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← เรียกโดย ReceiptFrame constructor เท่านั้น
 *     MessageLogger.save("========== RECEIPT ==========\n" + ...)
 *
 * ไฟล์ที่เกี่ยวข้อง:
 *   message.txt — เก็บประวัติ receipt ทุกครั้งที่มี checkout
 *                 ใช้ append mode → ข้อมูลเก่าไม่ถูกลบ
 */
public class MessageLogger {

    /**
     * FILE — ชื่อไฟล์ที่เก็บ log receipt (อยู่ใน working directory)
     * static final = constant ไม่เปลี่ยนแปลง
     */
    private static final String FILE = "message.txt";

    /**
     * save(String message) — บันทึกข้อความลงไฟล์ message.txt แบบ append
     *
     * FileWriter(FILE, true):
     *   true = append mode → เพิ่มต่อท้ายไฟล์ ไม่ overwrite
     *   ทำให้ message.txt สะสมประวัติ receipt ทุกครั้ง
     *
     * PrintWriter:
     *   wrap FileWriter เพื่อใช้ println() ได้สะดวก
     *
     * try-with-resources:
     *   ปิด PrintWriter อัตโนมัติเมื่อ block จบ (flush + close)
     *   ป้องกัน resource leak ถึงแม้จะมี Exception
     *
     * pw.println(message) — เขียนข้อความ + newline
     * pw.println("=================================") — เส้นคั่นระหว่าง receipt
     *
     * ข้อมูลที่ส่งมาจาก ReceiptFrame (รูปแบบ):
     * ─────────────────────────────────────────
     * ========== RECEIPT ==========
     * ========= ORDER LIST =========
     * Fried Rice x 2 = 100.0
     * Pad Thai x 1 = 60.0
     * ===============================
     * Total After Discount: 130.0
     * Cook Time: 34 minutes
     * Earned Points: 6
     * Total Points: 156
     * =============================
     * =================================   ← เพิ่มโดย MessageLogger
     * ─────────────────────────────────────────
     *
     * ใช้ใน:
     *   - ReceiptFrame constructor:
     *       MessageLogger.save(
     *           "========== RECEIPT ==========\n" +
     *           order.getOrderSummary() +
     *           "\nTotal After Discount: " + total + ...
     *       )
     *
     * @param message  ข้อความ receipt ที่ต้องการบันทึก
     */
    public static void save(String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE, true))) {
            pw.println(message);                       // เขียนเนื้อหา receipt
            pw.println("=================================");  // เส้นคั่น
        } catch (IOException e) {
            System.out.println("Error saving message: " + e.getMessage());
        }
    }
}
