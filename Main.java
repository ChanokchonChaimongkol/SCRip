package YummyList;

import javax.swing.*;

/**
 * ========================================================
 *  Main — จุดเริ่มต้นของโปรแกรม YummyList ทั้งหมด
 * ========================================================
 * เป็น class แรกที่ JVM เรียกใช้ผ่าน main()
 * หน้าที่หลัก:
 *   1. ตั้งค่า Look & Feel ให้ตรงกับ OS ที่รันอยู่
 *   2. สร้าง Object หลัก 2 ตัว (MemberManager, MenuManager)
 *   3. เปิดหน้าจอแรก LoginFrame
 *
 * ความสัมพันธ์กับ class อื่น:
 *   → สร้าง MemberManager  (จัดการข้อมูลสมาชิกทั้งหมด)
 *   → สร้าง MenuManager    (จัดการข้อมูลเมนูอาหารทั้งหมด)
 *   → ส่ง Object ทั้งสองเข้า LoginFrame เพื่อเริ่ม UI
 */
public class Main {

    /**
     * main() — entry point ของโปรแกรม
     *
     * @param args  argument จาก command line (ไม่ได้ใช้ในโปรแกรมนี้)
     */
    public static void main(String[] args) {

        /*
         * UIManager.setLookAndFeel(...)
         * ─────────────────────────────
         * ตั้งค่าธีมหน้าตาของ Swing ให้เหมือนกับ native OS
         *   - Windows → Windows style
         *   - macOS   → Aqua style
         *   - Linux   → GTK / Metal style
         * getSystemLookAndFeelClassName() คืนชื่อ class ของ L&F ที่เหมาะกับ OS
         * ถ้า set ไม่สำเร็จก็ข้ามไป (ใช้ Metal L&F ค่า default ของ Java แทน)
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // ไม่ต้องทำอะไร — ถ้าไม่ได้ก็ใช้ default L&F ต่อไป
        }

        /*
         * SwingUtilities.invokeLater(Runnable)
         * ─────────────────────────────────────
         * Swing ไม่ thread-safe → ต้องสร้างและอัปเดต GUI บน Event Dispatch Thread (EDT)
         * invokeLater() ส่ง Runnable เข้าคิวของ EDT ให้ทำงานทีหลัง
         * ทำให้ปลอดภัยจาก race condition ระหว่าง main thread กับ EDT
         *
         * Lambda () -> { ... } คือ Runnable ที่รันบน EDT:
         *   1. new MemberManager() → โหลดข้อมูลสมาชิกจาก member.txt (ถ้ามี)
         *   2. new MenuManager()   → โหลดข้อมูลเมนูจาก menu.txt (ถ้าไม่มีจะสร้าง default)
         *   3. new LoginFrame(...) → เปิดหน้าต่าง Login ให้ผู้ใช้
         */
        SwingUtilities.invokeLater(() -> new LoginFrame(new MemberManager(), new MenuManager()));
    }
}
