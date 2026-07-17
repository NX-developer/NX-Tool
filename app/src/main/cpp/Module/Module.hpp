#pragma once
#include <string>

enum class Category { Combat, Movement, Player, Render, World, Misc };

class Module {
public:
    Module(std::string name, Category category) : m_name(std::move(name)), m_category(category) {}
    virtual ~Module() = default;

    const std::string& getName() const { return m_name; }
    Category getCategory() const { return m_category; }
    bool isEnabled() const { return m_enabled; }

    void toggle() {
        m_enabled = !m_enabled;
        if (m_enabled) onEnable(); else onDisable();
    }

    virtual void onEnable() {}
    virtual void onDisable() {}
    virtual void onTick() {}

protected:
    std::string m_name;
    Category m_category;
    bool m_enabled = false;
};
