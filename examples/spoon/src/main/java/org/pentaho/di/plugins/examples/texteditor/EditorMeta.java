package org.pentaho.di.plugins.examples.texteditor;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObjectType;

import java.util.Date;

/**
 * EngineMetaInterface is the currency of Spoon. Your perspective will need to implement this interface in order to
 * participate in save/open operations. Most of this interface is left unimplemented and much of it doesn't apply to
 * modern usage.
 * <p/>
 * User: nbaker Date: 1/7/11
 */
public class EditorMeta implements EngineMetaInterface {


  private EditorModel model;

  public EditorMeta( EditorModel model ) {
    this.model = model;
  }

  public EditorModel getModel() {
    return model;
  }

  /**
   * Save is always on in the example. You can modify this to present more stateful behavior
   *
   * @return
   */
  public boolean canSave() {
    return true;
  }

  public String getDefaultExtension() {
    return "nte";
  }

  public String[] getFilterExtensions() {
    return new String[] { "*.nte" }; //$NON-NLS-1$
  }

  public String[] getFilterNames() {
    return new String[] { "Notes" };
  }

  public String getXML() throws KettleException {
    try {
      return "<message>" + getModel().getText() + "</message>";
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public String getFilename() {
    return getModel().getFileName();
  }

  public void setFilename( String filename ) {
    getModel().setText( filename );
  }

  // un-implemented methods below

  public void clearChanged() {
  }

  public Date getCreatedDate() {
    return null;
  }

  public void setCreatedDate( Date date ) {

  }

  public String getCreatedUser() {
    return null;
  }

  public void setCreatedUser( String createduser ) {

  }

  public String getFileType() {
    return null;
  }

  public Date getModifiedDate() {
    return null;
  }

  public void setModifiedDate( Date date ) {

  }

  public String getModifiedUser() {
    return null;
  }

  public void setModifiedUser( String user ) {

  }

  public String getName() {
    return getModel().getFileName();
  }

  public void setName( String name ) {

  }

  public void nameFromFilename() {

  }

  public void saveRep( Repository rep, ProgressMonitorListener monitor ) throws KettleException {

  }

  public void saveSharedObjects() {
  }

  public void setID( long id ) {

  }

  public void setInternalKettleVariables() {

  }

  public boolean showReplaceWarning( Repository rep ) {
    return false;
  }

  public RepositoryDirectoryInterface getRepositoryDirectory() {
    return null;
  }

  public void setRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectory ) {

  }

  public RepositoryObjectType getRepositoryElementType() {
    return null;
  }

  public String getDescription() {
    return null;
  }

  public void setDescription( String description ) {

  }

  public ObjectId getObjectId() {
    return null;
  }

  public void setObjectId( ObjectId id ) {

  }

  public ObjectRevision getObjectRevision() {
    return null;
  }

  public void setObjectRevision( ObjectRevision objectRevision ) {

  }
}
