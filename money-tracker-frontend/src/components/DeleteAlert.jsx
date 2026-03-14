import {useState} from "react";
import toast from "react-hot-toast";
import {LoaderCircle} from "lucide-react";

const DeleteAlert = ({content, onDelete}) => {
    const [loading, setLoading] = useState(false);
    const handleDelete = async () => {
        setLoading(true);
        try {
            await onDelete();
        }
        catch (error) {
            toast.error(error.response?.data?.message || "Failed to delete");
        } finally {
            setLoading(true);
        }
    }

    return (
        <div>
            <p className="text-sm">{content}</p>
            <div className="flex justify-end mt-6">
                <button
                    onClick={handleDelete}
                    disabled={loading}
                    type="button"
                    className="add-btn add-btn-fill">
                    {loading ? (
                        <>
                            <LoaderCircle className="h-4 w-4 animate-spin" />Deleting...
                        </>
                    ) : (
                        <>Delete</>
                    )}
                </button>
            </div>
        </div>
    )
}

export default DeleteAlert;