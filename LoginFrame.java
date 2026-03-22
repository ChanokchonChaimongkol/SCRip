package YummyList;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * ========================================================
 *  LoginFrame — หน้าต่างแรกของโปรแกรม (Login / Register / Admin)
 * ========================================================
 * extends JFrame → เป็นหน้าต่างอิสระ
 * มี 3 tab:
 *   1. Login    — สมาชิกเข้าสู่ระบบ
 *   2. Register — สมัครสมาชิกใหม่
 *   3. Admin    — Admin login (hardcoded user/pass)
 *
 * ความสัมพันธ์กับ class อื่น:
 *   ← สร้างโดย  Main.main()          → new LoginFrame(MemberManager, MenuManager)
 *   ← สร้างโดย  MemberFrame (logout) → dispose() แล้ว new LoginFrame(...)
 *   ← สร้างโดย  AdminFrame  (logout) → dispose() แล้ว new LoginFrame(...)
 *   → เปิด       MemberFrame  เมื่อ member login สำเร็จ
 *   → เปิด       AdminFrame   เมื่อ admin login สำเร็จ
 *   → เรียก      MemberManager.login(), register()
 */
public class LoginFrame extends JFrame {

    /** memberManager — ใช้สำหรับ login() และ register() + ส่งต่อให้ MemberFrame */
    private final MemberManager memberManager;

    /** menuManager — ไม่ได้ใช้โดยตรงใน LoginFrame แต่ส่งต่อให้ Frame ถัดไป */
    private final MenuManager menuManager;

    // ==================== Fields สำหรับ Login tab ====================
    /** loginUser — JTextField กรอก username สำหรับ member login */
    private JTextField loginUser;

    /** loginPass — JPasswordField กรอก password สำหรับ member login */
    private JPasswordField loginPass;

    // ==================== Fields สำหรับ Register tab ====================
    /** regUser    — JTextField กรอก username ใหม่ */
    private JTextField regUser;

    /** regPass    — JPasswordField กรอก password ใหม่ */
    private JPasswordField regPass;

    /** regConfirm — JPasswordField ยืนยัน password */
    private JPasswordField regConfirm;

    // ==================== Fields สำหรับ Admin tab ====================
    /** adminUser — JTextField กรอก admin username */
    private JTextField adminUser;

    /** adminPass — JPasswordField กรอก admin password */
    private JPasswordField adminPass;

    /**
     * ADMIN_USER, ADMIN_PASS — credentials admin แบบ hardcoded
     *
     * static final = constant ไม่เปลี่ยน, ใช้ร่วมกันทั้ง class
     * ใช้ใน doAdminLogin() เปรียบเทียบตรงๆ (ไม่ดึงจากไฟล์หรือ DB)
     */
    private static final String ADMIN_USER = "admin", ADMIN_PASS = "1234";

    /**
     * Constructor — สร้างหน้าต่าง Login พร้อม UI ทั้งหมด
     *
     * รับ MemberManager และ MenuManager เพื่อส่งต่อให้ Frame ถัดไป
     * ไม่ได้สร้าง Object เหล่านี้เอง → ใช้ Dependency Injection
     *
     * การสร้าง UI:
     *   1. ตั้งค่า JFrame (title, size, layout, background)
     *   2. สร้าง header panel สีส้มพร้อม logo + tagline
     *   3. สร้าง JTabbedPane ที่มี 3 tab (Login, Register, Admin)
     *   4. setVisible(true) → แสดงหน้าต่าง
     *
     * @param mm   MemberManager ที่สร้างมาจาก Main
     * @param men  MenuManager ที่สร้างมาจาก Main
     */
    public LoginFrame(MemberManager mm, MenuManager men) {
        this.memberManager = mm;
        this.menuManager   = men;

        setTitle("YummyList");
        setDefaultCloseOperation(EXIT_ON_CLOSE);   // ปิดโปรแกรมทั้งหมดเมื่อปิดหน้าต่าง
        setSize(440, 560);
        setLocationRelativeTo(null);   // กึ่งกลางหน้าจอ
        setResizable(false);           // ไม่ให้ resize
        getContentPane().setBackground(UI.BACKGROUND);
        setLayout(new BorderLayout());

        // Header — logo + tagline
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));   // 2 แถว, gap 4
        header.setBackground(UI.PRIMARY);   // พื้นหลังส้ม
        header.setBorder(new EmptyBorder(22, 0, 22, 0));

        JLabel logo = new JLabel("🍜 YummyList", SwingConstants.CENTER);   // CENTER = กึ่งกลาง
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setForeground(Color.WHITE);

        JLabel tag = new JLabel("Order your favorite food!", SwingConstants.CENTER);
        tag.setFont(UI.F_BODY);
        tag.setForeground(new Color(255, 220, 200));   // ขาวออกส้มอ่อน

        header.add(logo);
        header.add(tag);
        add(header, BorderLayout.NORTH);

        // JTabbedPane — 3 tab
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UI.F_BOLD);
        tabs.setBorder(new EmptyBorder(16, 16, 16, 16));   // padding รอบ tab pane
        tabs.addTab("  Login  ",    buildLoginTab());     // Tab 0
        tabs.addTab("  Register  ", buildRegisterTab());  // Tab 1
        tabs.addTab("  Admin  ",    buildAdminTab());     // Tab 2
        add(tabs, BorderLayout.CENTER);

        setVisible(true);
    }

    /**
     * buildLoginTab() — สร้าง panel สำหรับ tab Login
     *
     * สร้าง field loginUser, loginPass
     * สร้างปุ่ม "Login" (btnPrimary)
     *   ActionListener → doMemberLogin()
     * loginPass.addActionListener → กด Enter บน password field ก็ login ได้
     * ส่งทุกอย่างเข้า form() เพื่อจัดวางด้วย GridBagLayout
     *
     * @return JPanel tab login พร้อม UI
     */
    private JPanel buildLoginTab() {
        loginUser = UI.field();
        loginPass = UI.password();
        UI.Btn btn = UI.btnPrimary("Login");
        btn.setPreferredSize(new Dimension(0, UI.BTN_H));   // กว้างเต็ม, สูง BTN_H
        btn.addActionListener(e -> doMemberLogin());        // คลิกปุ่ม → login
        loginPass.addActionListener(e -> doMemberLogin());  // กด Enter → login
        return form(new Object[]{"Username", loginUser, "Password", loginPass, null, btn});
    }

    /**
     * buildRegisterTab() — สร้าง panel สำหรับ tab Register
     *
     * สร้าง field regUser, regPass, regConfirm
     * ปุ่ม "Create Account" (btnSuccess = เขียว)
     *   ActionListener → doRegister()
     *
     * @return JPanel tab register พร้อม UI
     */
    private JPanel buildRegisterTab() {
        regUser    = UI.field();
        regPass    = UI.password();
        regConfirm = UI.password();
        UI.Btn btn = UI.btnSuccess("Create Account");
        btn.setPreferredSize(new Dimension(0, UI.BTN_H));
        btn.addActionListener(e -> doRegister());
        return form(new Object[]{
            "Username", regUser,
            "Password", regPass,
            "Confirm Password", regConfirm,
            null, btn   // null = spacer
        });
    }

    /**
     * buildAdminTab() — สร้าง panel สำหรับ tab Admin Login
     *
     * สร้าง field adminUser, adminPass
     * ปุ่ม "Admin Login" (btnDanger = แดง)
     *   ActionListener → doAdminLogin()
     * adminPass.addActionListener → กด Enter บน password → admin login
     *
     * @return JPanel tab admin พร้อม UI
     */
    private JPanel buildAdminTab() {
        adminUser = UI.field();
        adminPass = UI.password();
        UI.Btn btn = UI.btnDanger("Admin Login");
        btn.setPreferredSize(new Dimension(0, UI.BTN_H));
        btn.addActionListener(e -> doAdminLogin());
        adminPass.addActionListener(e -> doAdminLogin());
        return form(new Object[]{"Admin Username", adminUser, "Admin Password", adminPass, null, btn});
    }

    /**
     * form(Object[] rows) — สร้าง JPanel จัดวาง label + component สลับกัน
     *
     * รับ array ที่สลับระหว่าง:
     *   String → สร้าง UI.label() (หัวข้อ)
     *   null   → สร้าง Box.createVerticalStrut(4) (ช่องว่าง)
     *   Component → ใช้ตรงๆ (input field หรือปุ่ม)
     *
     * GridBagLayout:
     *   c.fill = HORIZONTAL → ขยายเต็มความกว้าง
     *   c.weightx = 1       → แต่ละ row กว้างเท่ากัน
     *   c.gridx = 0         → column เดียว (vertical stack)
     *   c.gridy = i         → แต่ละ element อยู่คนละแถว
     *
     * @param rows  Array สลับ label/component
     * @return JPanel พร้อมจัดวาง
     */
    private JPanel form(Object[] rows) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UI.BACKGROUND);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        for (int i = 0; i < rows.length; i++) {
            c.gridy = i;
            // insets ต่างกันถ้าเป็น null (spacer)
            c.insets = (rows[i] == null) ? new Insets(12, 8, 6, 8) : new Insets(5, 8, 5, 8);
            p.add(
                rows[i] instanceof String  ? UI.label((String) rows[i])    // String → label
                : rows[i] == null          ? Box.createVerticalStrut(4)     // null → spacer
                : (Component) rows[i],                                      // Component → ใส่ตรงๆ
                c
            );
        }
        return p;
    }

    // ==================== Logic Methods ====================

    /**
     * doMemberLogin() — ประมวลผลการ login ของ member
     *
     * 1. อ่าน user, pass จาก loginUser, loginPass
     *    new String(loginPass.getPassword()) → แปลง char[] เป็น String
     * 2. ตรวจว่าไม่ว่าง → error ถ้าว่าง
     * 3. memberManager.login(user, pass) → ถ้าได้ Member ≠ null
     *    → dispose() (ปิด LoginFrame)
     *    → new MemberFrame(m, memberManager, menuManager) (เปิด MemberFrame)
     * 4. ถ้า null → แสดง error + ล้าง password field
     */
    private void doMemberLogin() {
        String user = loginUser.getText().trim();
        String pass = new String(loginPass.getPassword());   // char[] → String
        if (user.isEmpty() || pass.isEmpty()) { error("Please fill in all fields."); return; }
        Member m = memberManager.login(user, pass);
        if (m != null) {
            dispose();   // ปิด LoginFrame
            new MemberFrame(m, memberManager, menuManager);   // เปิด MemberFrame
        } else {
            error("Invalid username or password.");
            loginPass.setText("");   // ล้าง password field ให้ user พิมพ์ใหม่
        }
    }

    /**
     * doRegister() — ประมวลผลการสมัครสมาชิก
     *
     * Flow:
     * 1. อ่าน user, pass, conf จาก field ต่างๆ
     * 2. ตรวจ username ซ้ำ โดยอ่านไฟล์ member.txt โดยตรง:
     *    - ถ้าไม่มีไฟล์ (FileNotFoundException) → เรียก MemberManager.save("") สร้างไฟล์เปล่า
     *    - ถ้าพบ username ซ้ำ → warning + ล้าง fields + return
     * 3. ตรวจ field ว่าง
     * 4. ตรวจ password ตรงกับ confirm password
     * 5. memberManager.register(user, pass) → เพิ่มสมาชิกและบันทึกไฟล์
     * 6. แสดง success + ล้าง fields
     *
     * หมายเหตุ: การตรวจ username ซ้ำทำที่ UI layer (ไม่อยู่ใน MemberManager)
     *   เพราะ MemberManager.register() ไม่มีการ check ซ้ำ
     */
    private void doRegister() {
        String user = regUser.getText().trim();
        String pass = new String(regPass.getPassword());
        String conf = new String(regConfirm.getPassword());

        // ตรวจ username ซ้ำโดยอ่านไฟล์โดยตรง
        try {
            File file = new File("member.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split(",");
                String name = data[0];   // d[0] = username
                if (name.equals(user)) {
                    JOptionPane.showMessageDialog(this, "Username already taken!", "warning",
                            JOptionPane.WARNING_MESSAGE);
                    regUser.setText(""); regPass.setText(""); regConfirm.setText("");
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            // ไฟล์ยังไม่มี → สร้างไฟล์เปล่าก่อน
            MemberManager.save("");
        }

        if (user.isEmpty() || pass.isEmpty()) { error("Please fill in all fields."); return; }
        if (!pass.equals(conf)) { error("Passwords do not match."); return; }

        memberManager.register(user, pass);   // เพิ่มสมาชิกและบันทึกไฟล์
        JOptionPane.showMessageDialog(this, "Account created! You can now login.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        // ล้าง fields หลังสมัครสำเร็จ
        regUser.setText(""); regPass.setText(""); regConfirm.setText("");
    }

    /**
     * doAdminLogin() — ตรวจสอบ credentials admin และเปิด AdminFrame
     *
     * เปรียบเทียบกับ ADMIN_USER และ ADMIN_PASS โดยตรง (hardcoded)
     * ถ้าถูก → dispose() + new AdminFrame(menuManager, memberManager)
     * ถ้าผิด → error + ล้าง password field
     *
     * หมายเหตุ: AdminFrame รับ (menuManager, memberManager) ลำดับต่างจาก MemberFrame
     */
    private void doAdminLogin() {
        String user = adminUser.getText().trim();
        String pass = new String(adminPass.getPassword());
        if (user.equals(ADMIN_USER) && pass.equals(ADMIN_PASS)) {
            dispose();
            new AdminFrame(menuManager, memberManager);   // เปิด AdminFrame
        } else {
            error("Invalid admin credentials.");
            adminPass.setText("");
        }
    }

    /**
     * error(String msg) — แสดง dialog แจ้ง error
     *
     * JOptionPane.showMessageDialog:
     *   this = parent component (กึ่งกลาง LoginFrame)
     *   ERROR_MESSAGE = ไอคอน ✖
     *
     * @param msg ข้อความที่จะแสดง
     */
    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
