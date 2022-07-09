-- ------------------------------------------------------------------------------
--
-- ---------------------- FUNCTION / TRIGGER DEFINITIONS ------------------------

-- Trigger for the links table to update modified timestamp using the above
-- function.
DROP TRIGGER IF EXISTS update_link_modified $$

CREATE TRIGGER update_link_modified AFTER UPDATE ON links
FOR EACH ROW
  BEGIN
    UPDATE links set modified = (datetime('now', 'utc')) WHERE id = old.id;
  END $$

