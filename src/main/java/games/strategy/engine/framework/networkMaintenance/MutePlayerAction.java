package games.strategy.engine.framework.networkMaintenance;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import games.strategy.net.INode;
import games.strategy.net.IServerMessenger;

public class MutePlayerAction extends AbstractAction {
  private static final long serialVersionUID = -6578758359870435844L;
  private final Component m_parent;
  private final IServerMessenger m_messenger;

  public MutePlayerAction(final Component parent, final IServerMessenger messenger) {
    super("Mute Player's Chatting");
    m_parent = JOptionPane.getFrameForComponent(parent);
    m_messenger = messenger;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    final JComboBox<String> combo = new JComboBox<>(model);
    model.addElement("");
    for (final INode node : new TreeSet<>(m_messenger.getNodes())) {
      if (!node.equals(m_messenger.getLocalNode())) {
        model.addElement(node.getName());
      }
    }
    if (model.getSize() == 1) {
      JOptionPane.showMessageDialog(m_parent, "No remote players", "No Remote Players", JOptionPane.ERROR_MESSAGE);
      return;
    }
    final int selectedOption =
        JOptionPane.showConfirmDialog(m_parent, combo, "Select player to mute", JOptionPane.OK_CANCEL_OPTION);
    if (selectedOption != JOptionPane.OK_OPTION) {
      return;
    }
    final String name = (String) combo.getSelectedItem();
    for (final INode node : m_messenger.getNodes()) {
      if (node.getName().equals(name)) {
        final String realName = node.getName().split(" ")[0];
        final String ip = node.getAddress().getHostAddress();
        final String mac = m_messenger.getPlayerMac(node.getName());
        m_messenger.notifyUsernameMutingOfPlayer(realName, null);
        m_messenger.notifyIpMutingOfPlayer(ip, null);
        m_messenger.notifyMacMutingOfPlayer(mac, null);
        return;
      }
    }
  }
}
