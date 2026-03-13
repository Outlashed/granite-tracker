package com.github.outlashed.granitetracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import net.runelite.client.config.ConfigManager;
import com.github.outlashed.granitetracker.model.ProfileData;
import com.github.outlashed.granitetracker.services.GraniteTrackingService;
import com.github.outlashed.granitetracker.services.ProfileStorageService;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

@Singleton
public class GraniteTrackerPanel extends PluginPanel
{
    private static final Color COLOR_DANGER  = new Color(0xC0, 0x39, 0x2B);
    private static final Color COLOR_SUCCESS = new Color(0x27, 0x7A, 0x50);
    private static final Color COLOR_INACTIVE = ColorScheme.MEDIUM_GRAY_COLOR;

    private final GraniteTrackingService graniteTrackingService;
    private final ProfileStorageService profileStorageService;
    private final ConfigManager configManager;
    private final GraniteTrackerConfig config;

    private final JTextField profileNameField = new JTextField();
    private final JComboBox<String> profileDropdown = new JComboBox<>();

    private JButton generalStatsToggle;
    private JButton graniteOverlayToggle;

    @Inject
    public GraniteTrackerPanel(GraniteTrackingService graniteTrackingService,
                               ProfileStorageService profileStorageService,
                               ConfigManager configManager,
                               GraniteTrackerConfig config)
    {
        this.graniteTrackingService = graniteTrackingService;
        this.profileStorageService = profileStorageService;
        this.configManager = configManager;
        this.config = config;
        buildUI();
    }

    private void buildUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(0, 0, 0, 0));

        add(buildTitleBar());
        add(buildSaveSection());
        add(buildLoadSection());
        add(buildOverlaySection());
        add(buildResetSection());
    }

    // ── Title ────────────────────────────────────────────────────────────────

    private JPanel buildTitleBar()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, ColorScheme.BRAND_ORANGE.darker()),
                new EmptyBorder(10, 12, 10, 12)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel title = new JLabel("Granite Tracker");
        title.setForeground(Color.WHITE);
        title.setFont(FontManager.getRunescapeFont());
        panel.add(title, BorderLayout.WEST);

        return panel;
    }

    // ── Save profile ─────────────────────────────────────────────────────────

    private JPanel buildSaveSection()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, ColorScheme.DARKER_GRAY_COLOR),
                new EmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        panel.add(buildSectionLabel("PROFILES"));
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel nameLabel = new JLabel("Profile name");
        nameLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        nameLabel.setFont(FontManager.getRunescapeFont());
        nameLabel.setHorizontalAlignment(JLabel.LEFT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        panel.add(nameLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        styleTextField(profileNameField);
        panel.add(profileNameField);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        JButton saveButton = buildButton("Save Current Session", COLOR_SUCCESS);
        saveButton.addActionListener(e -> saveProfile());
        panel.add(saveButton);

        return panel;
    }

    // ── Load / Delete profile ────────────────────────────────────────────────

    private JPanel buildLoadSection()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, ColorScheme.DARKER_GRAY_COLOR),
                new EmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        panel.add(buildSectionLabel("SAVED PROFILES"));
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        styleComboBox(profileDropdown);
        refreshProfileDropdown();
        panel.add(profileDropdown);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        JButton loadButton   = buildButton("Load",   COLOR_SUCCESS);
        JButton deleteButton = buildButton("Delete", COLOR_DANGER);
        loadButton.addActionListener(e -> loadProfile());
        deleteButton.addActionListener(e -> deleteProfile());

        JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 0));
        buttons.setBackground(ColorScheme.DARK_GRAY_COLOR);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        buttons.add(loadButton);
        buttons.add(deleteButton);
        panel.add(buttons);

        return panel;
    }

    // ── Overlay toggles ──────────────────────────────────────────────────────

    private JPanel buildOverlaySection()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, ColorScheme.DARKER_GRAY_COLOR),
                new EmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        panel.add(buildSectionLabel("OVERLAYS"));
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        generalStatsToggle = buildToggleButton("General Statistics", config.showGeneralStats());
        generalStatsToggle.addActionListener(e ->
        {
            boolean newVal = !config.showGeneralStats();
            configManager.setConfiguration("granitetracker", "showGeneralStats", newVal);
            applyToggleColor(generalStatsToggle, newVal);
        });
        panel.add(generalStatsToggle);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        graniteOverlayToggle = buildToggleButton("Granite Distribution", config.showGraniteOverlay());
        graniteOverlayToggle.addActionListener(e ->
        {
            boolean newVal = !config.showGraniteOverlay();
            configManager.setConfiguration("granitetracker", "showGraniteOverlay", newVal);
            applyToggleColor(graniteOverlayToggle, newVal);
        });
        panel.add(graniteOverlayToggle);

        return panel;
    }

    // ── Reset ────────────────────────────────────────────────────────────────

    private JPanel buildResetSection()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, ColorScheme.DARKER_GRAY_COLOR),
                new EmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JButton resetButton = buildButton("Reset Statistics", COLOR_DANGER);
        resetButton.addActionListener(e -> graniteTrackingService.reset());
        panel.add(resetButton);

        return panel;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JLabel buildSectionLabel(String text)
    {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setForeground(ColorScheme.BRAND_ORANGE);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        return label;
    }

    private JButton buildButton(String text, Color baseColor)
    {
        Color hoverColor = baseColor.brighter();
        Color pressColor = baseColor.darker();

        JButton button = new JButton(text);
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setFont(FontManager.getRunescapeFont());
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        button.addMouseListener(new MouseAdapter()
        {
            @Override public void mouseEntered(MouseEvent e)  { button.setBackground(hoverColor); }
            @Override public void mouseExited(MouseEvent e)   { button.setBackground(baseColor);  }
            @Override public void mousePressed(MouseEvent e)  { button.setBackground(pressColor); }
            @Override public void mouseReleased(MouseEvent e) { button.setBackground(hoverColor); }
        });

        return button;
    }

    private void styleTextField(JTextField field)
    {
        field.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(FontManager.getRunescapeFont());
        field.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, ColorScheme.BRAND_ORANGE.darker()),
                new EmptyBorder(4, 6, 4, 6)
        ));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
    }

    private void styleComboBox(JComboBox<String> combo)
    {
        combo.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        combo.setForeground(Color.WHITE);
        combo.setFont(FontManager.getRunescapeFont());
        combo.setBorder(new MatteBorder(0, 0, 1, 0, ColorScheme.BRAND_ORANGE.darker()));
        combo.setAlignmentX(Component.CENTER_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        combo.setRenderer(new DefaultListCellRenderer()
        {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus)
            {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected
                        ? ColorScheme.BRAND_ORANGE.darker()
                        : ColorScheme.DARKER_GRAY_COLOR);
                setForeground(Color.WHITE);
                setFont(FontManager.getRunescapeFont());
                setBorder(new EmptyBorder(4, 6, 4, 6));
                return this;
            }
        });
    }

    private JButton buildToggleButton(String text, boolean active)
    {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setFont(FontManager.getRunescapeFont());
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        applyToggleColor(button, active);
        return button;
    }

    private void applyToggleColor(JButton button, boolean active)
    {
        button.setBackground(active ? COLOR_SUCCESS : COLOR_INACTIVE);
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void saveProfile()
    {
        String name = profileNameField.getText().trim();
        if (name.isEmpty())
        {
            return;
        }
        profileStorageService.saveProfile(name, graniteTrackingService);
        refreshProfileDropdown();
        profileNameField.setText("");
    }

    private void loadProfile()
    {
        String name = (String) profileDropdown.getSelectedItem();
        if (name == null)
        {
            return;
        }
        ProfileData data = profileStorageService.loadProfile(name);
        if (data != null)
        {
            graniteTrackingService.loadFromProfile(data);
        }
    }

    private void deleteProfile()
    {
        String name = (String) profileDropdown.getSelectedItem();
        if (name == null)
        {
            return;
        }
        profileStorageService.deleteProfile(name);
        refreshProfileDropdown();
    }

    private void refreshProfileDropdown()
    {
        profileDropdown.removeAllItems();
        for (String name : profileStorageService.getProfileNames())
        {
            profileDropdown.addItem(name);
        }
    }
}
