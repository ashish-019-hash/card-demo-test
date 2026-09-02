import { useNavigate } from 'react-router-dom';
import { PageHeader } from '../components/Layout';

const options = [
  ['/users/add', '1. Add user', 'Create a user profile and assign its access type.'],
  ['/users/update', '2. Update user', 'Look up a user and change their profile details.'],
  ['/users/delete', '3. Delete user', 'Look up and permanently remove a user.'],
] as const;

export function AdminHomePage() {
  const navigate = useNavigate();
  return <div className="content"><PageHeader title="Admin menu" description="Choose a user-management task." /><div className="menugrid">{options.map(([path, title, detail]) => <button key={path} className="tile" onClick={() => navigate(path)}><div className="tileicon">{title[0]}</div><h2>{title}</h2><p>{detail}</p><span className="go">Open task →</span></button>)}</div></div>;
}
